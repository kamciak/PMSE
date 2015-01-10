package com.publicationmetasearchengine.gui.notificationcriteriasscreen;

import com.publicationmetasearchengine.dao.filtercriterias.exceptions.FilterCriteriasDoesNotExistException;
import com.publicationmetasearchengine.dao.users.exceptions.UserDoesNotExistException;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.data.filters.FilterCriteria;
import com.publicationmetasearchengine.data.filters.FilterType;
import com.publicationmetasearchengine.data.filters.NamedFilterCriteria;
import com.publicationmetasearchengine.data.filters.exceptions.UnsupportedFilterTypeException;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.gui.searchscreen.components.AndOrFilterPanel;
import com.publicationmetasearchengine.gui.searchscreen.components.Filter;
import com.publicationmetasearchengine.gui.searchscreen.components.ListFiterPanel;
import com.publicationmetasearchengine.management.filtercriteriasmanagement.FilterCriteriaManager;
import com.publicationmetasearchengine.utils.Notificator;
import com.publicationmetasearchengine.utils.validable.ValidationException;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
public abstract class FiltersPanel  extends PMSEPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(FiltersPanel.class);

    private final TextField filtersNameEd = new TextField("Name");

    private final AndOrFilterPanel titleFilterPanel = new AndOrFilterPanel("Title", FilterType.TITLE);
    private final AndOrFilterPanel summrayFilterPanel = new AndOrFilterPanel("Summary", FilterType.SUMMARY);
    private final ListFiterPanel authorFilterPanel = new ListFiterPanel("Author", FilterType.AUTHOR);
    private final List<Filter> filters = new ArrayList<Filter>();
    {
        filters.add(titleFilterPanel);
        filters.add(summrayFilterPanel);
        filters.add(authorFilterPanel);
    };

    @Autowired
    private FilterCriteriaManager filterCriteriaManager;

    private final PMSEButton saveBtn = new PMSEButton("Save");
    private final PMSEButton clearBtn = new PMSEButton("Clear");
    private Integer filtersId = null;

    public FiltersPanel(String caption) {
        super(caption);
        setSizeUndefined();
        GridLayout mainLayout = new GridLayout(2, 4);
        mainLayout.setSizeUndefined();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        mainLayout.addComponent(filtersNameEd, 0, 0);

        for (int i = 0; i < filters.size(); ++i)
            mainLayout.addComponent(filters.get(i), i%2, i/2 + 1);

        saveBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    validate();
                    String name = filtersNameEd.getValue().toString();
                    User user = (User) getApplication().getUser();
                    NamedFilterCriteria namedFilterCriteria = new NamedFilterCriteria(getFiltersCriteria(), name);
                    if (filtersId == null)
                        filtersId = filterCriteriaManager.saveNewFilterCriterias(user, namedFilterCriteria);
                    else {
                       namedFilterCriteria.setId(filtersId);
                       filterCriteriaManager.saveModifiedFilterCriterias(user, namedFilterCriteria);
                    }
                    additionalSaveBtnClick();
                } catch (ValidationException ex) {
                    Notificator.showNotification(getApplication(), "Validation error", ex.getMessage(), Notificator.NotificationType.ERROR);
                } catch (UserDoesNotExistException ex) {
                    LOGGER.fatal("Should not occure!!!", ex);
                    Notificator.showNotification(getApplication(), "Error", "Internal error.\nTry again later.\nIf it repeates, please notify administrator.", Notificator.NotificationType.ERROR);
                } catch (FilterCriteriasDoesNotExistException ex) {
                    LOGGER.fatal("Should not occure!!!", ex);
                    Notificator.showNotification(getApplication(), "Error", "Internal error.\nTry again later.\nIf it repeates, please notify administrator.", Notificator.NotificationType.ERROR);
                }

            }
        });

        clearBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                for (Filter filter : filters)
                    filter.clear();
            }
        });

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.addComponent(saveBtn);
        buttonLayout.addComponent(clearBtn);
        mainLayout.addComponent(buttonLayout, 1, 3);
        mainLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_RIGHT);

        setContent(mainLayout);
        setEnabled(false);
    }

    public List<FilterCriteria> getFiltersCriteria(){
        ArrayList<FilterCriteria> criterias = new ArrayList<FilterCriteria>();
        for (Filter f : filters)
            criterias.add(f.getFilterCriteria());
        return criterias;
    }

    public void setFilters(NamedFilterCriteria namedFilterCriteria) throws UnsupportedFilterTypeException {
        clearBtn.click();
        this.setEnabled(namedFilterCriteria!= null);
        if (namedFilterCriteria == null) {
            filtersId = null;
            return ;
        }
        filtersId = namedFilterCriteria.getId();
        filtersNameEd.setValue(namedFilterCriteria.getName());
        List<FilterCriteria> filterCriterias = namedFilterCriteria.getFilters();
        if (filterCriterias == null) return;
        for (FilterCriteria filterCriteria : filterCriterias) {
            switch (filterCriteria.getFilterType()) {
                case AUTHOR: authorFilterPanel.setFilterCriteria(filterCriteria); break;
                case SUMMARY: summrayFilterPanel.setFilterCriteria(filterCriteria); break;
                case TITLE: titleFilterPanel.setFilterCriteria(filterCriteria); break;
                default: throw new UnsupportedFilterTypeException(filterCriteria.getFilterType().toString());
            }
        }
    }

    public void validate() throws ValidationException {
        if (filtersNameEd.getValue() == null || filtersNameEd.getValue().equals(""))
            throw new ValidationException("Filter name can not be empty");
    }

    public abstract void additionalSaveBtnClick();
}
