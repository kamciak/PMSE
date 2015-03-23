package com.publicationmetasearchengine.gui.searchscreen;

import com.publicationmetasearchengine.gui.searchscreen.components.DateFilterPanel;
import com.publicationmetasearchengine.data.filters.FilterCriteria;
import com.publicationmetasearchengine.data.filters.FilterType;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.gui.searchscreen.components.AndOrFilterPanel;
import com.publicationmetasearchengine.gui.searchscreen.components.Filter;
import com.publicationmetasearchengine.gui.searchscreen.components.ListFiterPanel;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.List;

public abstract class FiltersPanel  extends PMSEPanel {
    private static final long serialVersionUID = 1L;

    private final AndOrFilterPanel titleFilterPanel = new AndOrFilterPanel("Title", FilterType.TITLE);
    private final AndOrFilterPanel summrayFilterPanel = new AndOrFilterPanel("Summary", FilterType.SUMMARY);
    private final DateFilterPanel dateFilterPanel = new DateFilterPanel("Publication date", FilterType.PUBLICATION_DATE);
    private final ListFiterPanel authorFilterPanel = new ListFiterPanel("Author", FilterType.AUTHOR);
    private final ListFiterPanel doiFilterPanel = new ListFiterPanel("DOI", FilterType.DOI);
    private final ListFiterPanel journalFilterPanel = new ListFiterPanel("Journal", FilterType.JOURNAL);
    private final List<Filter> filters = new ArrayList<Filter>();
    {
        filters.add(authorFilterPanel);
        filters.add(titleFilterPanel);
        filters.add(summrayFilterPanel);
        filters.add(dateFilterPanel);
        filters.add(journalFilterPanel);
        filters.add(doiFilterPanel);
    };

    private final PMSEButton searchBtn = new PMSEButton("Search");
    private final PMSEButton clearBtn = new PMSEButton("Clear");

    public FiltersPanel(String caption) {
        super(caption);
        setSizeFull();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        VerticalLayout filterLayout = new VerticalLayout();
        filterLayout.setWidth("100%");
        filterLayout.setMargin(false, true, true, false);
        filterLayout.setSpacing(true);

        for (Filter filter : filters) {
            filterLayout.addComponent(filter);
            filter.setWidth("100%");
        }

        searchBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                searchClick();
            }
        });
        searchBtn.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        clearBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                for (Filter filter : filters)
                    filter.clear();
            }
        });
        Panel filterLayoutPanel = new Panel();
        filterLayoutPanel.setContent(filterLayout);
        filterLayoutPanel.setSizeFull();
        filterLayoutPanel.setStyleName("borderless");
        mainLayout.addComponent(filterLayoutPanel);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addComponent(clearBtn);
        buttonLayout.addComponent(searchBtn);
        buttonLayout.setSpacing(true);

        mainLayout.addComponent(buttonLayout);
        mainLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_RIGHT);
        mainLayout.setExpandRatio(filterLayoutPanel, 1);
        setContent(mainLayout);
    }

    public List<FilterCriteria> getFiltersCriteria(){
        ArrayList<FilterCriteria> criterias = new ArrayList<FilterCriteria>();
        for (Filter f : filters)
            criterias.add(f.getFilterCriteria());
        return criterias;
    }

    abstract public void searchClick();

}
