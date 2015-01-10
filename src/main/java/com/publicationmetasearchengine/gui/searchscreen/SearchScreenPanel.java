package com.publicationmetasearchengine.gui.searchscreen;

import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.data.filters.FilterCriteria;
import com.publicationmetasearchengine.gui.ScreenPanel;
import com.publicationmetasearchengine.gui.homescreen.PreviewPanel;
import com.publicationmetasearchengine.gui.mainmenu.MainMenuBar;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.management.publicationmanagement.PublicationManager;
import com.vaadin.data.Property;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
public class SearchScreenPanel extends VerticalLayout implements ScreenPanel {
    private static final long serialVersionUID = 1L;

    @Autowired
    PublicationManager publicationManager;

    private final MainMenuBar menuBar = new MainMenuBar();

    private FiltersPanel filtersPanel = new FiltersPanel("Filters") {
        private static final long serialVersionUID = 1L;

        @Override
        public void searchClick() {
            final List<FilterCriteria> filtersCriteria = getFiltersCriteria();
            final List<Publication> publications = publicationManager.getPublicationsMatchingFiltersCriteria(filtersCriteria);
            resultTable.clear();
            resultTable.addPublications(publications);
        }
    };

    private PMSEPanel resultPanel = new PMSEPanel("Result");
    private ResultTable resultTable = new ResultTable();
    private PreviewPanel previewPanel = new PreviewPanel("Content");
    private HorizontalLayout mainHorizontalLayout;
    boolean isPreviewVisible = false;

    public SearchScreenPanel() {
        super();
        setMargin(true);
        setSpacing(true);
        setSizeFull();

        mainHorizontalLayout = initMainHorizontalLayout();

        addComponent(menuBar);
        addComponent(mainHorizontalLayout);
        setExpandRatio(menuBar, 0);
        setExpandRatio(mainHorizontalLayout, 1);
    }

    private HorizontalLayout initMainHorizontalLayout() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setMargin(true);
        hl.setSpacing(true);
        hl.setSizeFull();

        filtersPanel.setHeight("100%");
        filtersPanel.setWidth("320px");
        initResultPanel();
        previewPanel.setSizeFull();


        hl.addComponent(filtersPanel);
        hl.addComponent(resultPanel);
        hl.setExpandRatio(resultPanel, 3);

        return hl;
    }

    private void initResultPanel() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        vl.setSpacing(true);

        resultTable.setSizeFull();
        resultTable.setSizeFull();
        resultTable.setSelectable(true);
        resultTable.setImmediate(true);
        resultTable.addListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Object id = event.getProperty().getValue();
                if (id == null) {
                    setPreviewPanelVisibility(false);
                    return;
                }

                if (!isPreviewVisible) {
                    setPreviewPanelVisibility(true);
                }
                Publication publication = (Publication) resultTable.getItem(id).getItemProperty(ResultTable.TABLE_PUBLICATION_COLUMN).getValue();
                previewPanel.setContent(publication);
            }
        });
        vl.addComponent(resultTable);

        resultPanel.setContent(vl);
        resultPanel.setSizeFull();
    }

    private void setPreviewPanelVisibility(boolean visible) {
        isPreviewVisible = visible;
        if (visible) {
            mainHorizontalLayout.addComponent(previewPanel);
            mainHorizontalLayout.setExpandRatio(previewPanel, 2);
        } else {
            mainHorizontalLayout.removeComponent(previewPanel);
        }
    }
}
