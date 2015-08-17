/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.gui.searchjournalscreen;

import com.publicationmetasearchengine.data.filters.FilterCriteria;
import com.publicationmetasearchengine.data.filters.FilterType;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.gui.searchscreen.components.AllOptionFilterPanel;
import com.publicationmetasearchengine.gui.searchscreen.components.AndOrFilterPanel;
import com.publicationmetasearchengine.gui.searchscreen.components.Filter;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kamciak
 */
public abstract class FiltersJournalPanel extends PMSEPanel {
    private static final long serialVersionUID = 1L;

    private final AllOptionFilterPanel titleFilterPanel = new AllOptionFilterPanel("Title", FilterType.TITLE_JOURNAL);
    private final AllOptionFilterPanel abstractFilterPanel = new AllOptionFilterPanel("Abstract", FilterType.ABSTRACT_JOURNAL);
    private final List<Filter> filters = new ArrayList<Filter>();
    private VerticalLayout filterLayout = new VerticalLayout();
    {
        filters.add(titleFilterPanel);
        filters.add(abstractFilterPanel);
    };

    private final PMSEButton searchBtn = new PMSEButton("Search");
    private final PMSEButton clearBtn = new PMSEButton("Clear");

    public FiltersJournalPanel(String caption) {
        super(caption);
        setSizeFull();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        
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
