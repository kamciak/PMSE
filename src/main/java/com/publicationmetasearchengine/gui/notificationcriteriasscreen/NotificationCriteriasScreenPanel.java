package com.publicationmetasearchengine.gui.notificationcriteriasscreen;

import com.publicationmetasearchengine.gui.ScreenPanel;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

public class NotificationCriteriasScreenPanel extends VerticalLayout implements ScreenPanel {

    private static final long serialVersionUID = 1L;
    private PMSEPanel listPanel = new PMSEPanel("Saved search criterias");
    private FilterCriteriasList filterCriteriasList;
    private FiltersPanel filtersPanel = new FiltersPanel("Filters") {
        @Override
        public void additionalSaveBtnClick() {
            filterCriteriasList.refresh();
        }
    };

    public NotificationCriteriasScreenPanel() {
        super();
        filterCriteriasList = new FilterCriteriasList(filtersPanel);
        setMargin(true);
        setSpacing(true);
        setSizeFull();

        initFiltersList();
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.addComponent(listPanel);
        mainLayout.addComponent(filtersPanel);
        mainLayout.setExpandRatio(listPanel, 1);
        mainLayout.setExpandRatio(filtersPanel, 2);

        addComponent(mainLayout);
        setExpandRatio(mainLayout, 1);
    }

    private void initFiltersList() {
        listPanel.setSizeFull();

        listPanel.setContent(filterCriteriasList);
    }
}
