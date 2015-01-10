package com.publicationmetasearchengine.gui.notificationcriteriasscreen;

import com.publicationmetasearchengine.dao.filtercriterias.exceptions.FilterCriteriasDoesNotExistException;
import com.publicationmetasearchengine.dao.users.exceptions.UserDoesNotExistException;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.data.filters.NamedFilterCriteria;
import com.publicationmetasearchengine.data.filters.exceptions.UnsupportedFilterTypeException;
import com.publicationmetasearchengine.gui.ConfirmWindow;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.management.filtercriteriasmanagement.FilterCriteriaManager;
import com.publicationmetasearchengine.utils.Notificator;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
public class FilterCriteriasList extends VerticalLayout{
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(FilterCriteriasList.class);

    public static final String CONTENT_TABLE_NAME_COLUMN = "Name";
    public static final String CONTENT_TABLE_FILTERS_COLUMN = "Filters";
    private static final String[] CONTENT_TABLE_VISIBLE_COLUMNS = {
        CONTENT_TABLE_NAME_COLUMN
    };

    @Autowired
    private FilterCriteriaManager filterCriteriaManager;

    private Table contentTable = new Table();

    private HorizontalLayout buttonsLayout;
    private PMSEButton newBtn = new PMSEButton("New");
    private PMSEButton deleteBtn = new PMSEButton("Delete");

    private final FiltersPanel filtersPanel;

    public FilterCriteriasList(final FiltersPanel filtersPanel) {
        super();
        this.filtersPanel = filtersPanel;
        setSizeFull();
        setSpacing(true);
        setMargin(true);

        contentTable.setSizeFull();
        contentTable.addContainerProperty(CONTENT_TABLE_NAME_COLUMN, String.class, "");
        contentTable.addContainerProperty(CONTENT_TABLE_FILTERS_COLUMN, NamedFilterCriteria.class, null);
        contentTable.setVisibleColumns(CONTENT_TABLE_VISIBLE_COLUMNS);
        contentTable.setSelectable(true);
        contentTable.setImmediate(true);
        contentTable.addListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Object itemId = contentTable.getValue();
                NamedFilterCriteria filterCriteria = itemId==null?null:(NamedFilterCriteria) contentTable.getItem(itemId).getItemProperty(CONTENT_TABLE_FILTERS_COLUMN).getValue();
                try {
                    filtersPanel.setFilters(filterCriteria);
                } catch (UnsupportedFilterTypeException ex) {
                    LOGGER.fatal("Should not occure!!!", ex);
                    Notificator.showNotification(getApplication(), "Error", "Internal error.\nTry again later.\nIf it repeates, please notify administrator.", Notificator.NotificationType.ERROR);
                }
            }
        });

        buttonsLayout = initButtonsLayout();

        addComponent(contentTable);
        addComponent(buttonsLayout);
        setExpandRatio(contentTable, 1);
        setExpandRatio(buttonsLayout, 0);
        setComponentAlignment(buttonsLayout, Alignment.MIDDLE_RIGHT);
    }

    @Override
    public void attach() {
        super.attach();
        refresh();
    }

    private HorizontalLayout initButtonsLayout() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.setSizeUndefined();

        newBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    filtersPanel.setFilters(new NamedFilterCriteria(null, "New filter criteria"));
                } catch (UnsupportedFilterTypeException ex) {
                }
            }
        });

        deleteBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                final Object itemId = contentTable.getValue();
                if (itemId == null) return ;
                String name = (String) contentTable.getItem(itemId).getItemProperty(CONTENT_TABLE_NAME_COLUMN).getValue();
                new ConfirmWindow(getApplication(), "Deleting filter criterias", String.format("Do you really want to delete [%s] criterias?", name)) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void yesButtonClick() {
                        NamedFilterCriteria nfc = (NamedFilterCriteria) contentTable.getItem(itemId).getItemProperty(CONTENT_TABLE_FILTERS_COLUMN).getValue();
                        try {
                            filterCriteriaManager.deleteFilterCriterias((User) getApplication().getUser(), nfc.getId());
                            contentTable.removeItem(itemId);
                        } catch (UserDoesNotExistException ex) {
                            LOGGER.fatal(ex);
                        } catch (FilterCriteriasDoesNotExistException ex) {
                            LOGGER.fatal(ex);
                        }
                    }
                };
            }
        });

        hl.addComponent(newBtn);
        hl.addComponent(deleteBtn);
        return hl;
    }

    public void refresh() {
        contentTable.removeAllItems();
        try {
            for(NamedFilterCriteria nfc : filterCriteriaManager.getUsersFilterCriterias((User)getApplication().getUser())) {
                Object item = contentTable.addItem();
                contentTable.getItem(item).getItemProperty(CONTENT_TABLE_NAME_COLUMN).setValue(nfc.getName());
                contentTable.getItem(item).getItemProperty(CONTENT_TABLE_FILTERS_COLUMN).setValue(nfc);
            }
        } catch (UserDoesNotExistException ex) {
            LOGGER.fatal("Should not occure!!!", ex);
            Notificator.showNotification(getApplication(), "Error", "Internal error.\nTry again later.\nIf it repeates, please notify administrator.", Notificator.NotificationType.ERROR);
        }

    }

}
