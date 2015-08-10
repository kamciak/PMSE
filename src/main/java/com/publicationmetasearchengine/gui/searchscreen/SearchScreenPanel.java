package com.publicationmetasearchengine.gui.searchscreen;

import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.data.filters.FilterCriteria;
import com.publicationmetasearchengine.gui.PublicationScreenPanel;
import com.publicationmetasearchengine.gui.ScreenPanel;
import com.publicationmetasearchengine.gui.homescreen.PreviewPanel;
import com.publicationmetasearchengine.gui.mainmenu.MainMenuBarAuthorizedUser;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.management.backupmanagement.BackupManager;
import com.publicationmetasearchengine.management.publicationmanagement.PublicationManager;
import com.publicationmetasearchengine.utils.Notificator;
import com.vaadin.data.Property;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
public class SearchScreenPanel extends CustomComponent implements PublicationScreenPanel {
    private static final Logger LOGGER = Logger.getLogger(SearchScreenPanel.class);
    private static final long serialVersionUID = 1L;
    private boolean isExternalPublication = false;
    @Autowired
    PublicationManager publicationManager;
    @Autowired
    private BackupManager backupManager;

    //private final MainMenuBarAuthorizedUser menuBar = new MainMenuBarAuthorizedUser();
    private List<Publication> allPublications = new ArrayList<Publication>();

    private FiltersPanel filtersPanel = new FiltersPanel("Filters") {
        private static final long serialVersionUID = 1L;

        @Override
        public void searchClick() {
                final List<FilterCriteria> filtersCriteria = getFiltersCriteria();
                if(!isFilterCriteriaEmpty(filtersCriteria)){
                    final List<Publication> publications = publicationManager.getPublicationsMatchingFiltersCriteria(filtersCriteria);
                    resultTable.clear();
                    resultTable.addPublications(publications);
                } else {
                    Notificator.showNotification(getApplication(), "No filter criteria", "\nEmpty filter criteria. Please fill any of field.", Notificator.NotificationType.HUMANIZED);
                }
        }
    };

    private PMSEPanel resultPanel = new PMSEPanel("Result");
    private ResultTable resultTable = new ResultTable();
    private PreviewPanel previewPanel = new PreviewPanel("Content");
    private HorizontalLayout mainHorizontalLayout;
    boolean isPreviewVisible = false;

    public SearchScreenPanel() {
        super();
        initSearchScreenPanel();
        setCompositionRoot(mainHorizontalLayout);
    }
    
    public SearchScreenPanel(boolean isExternalPublication)
    {
        super();
        initSearchScreenPanel();
        this.isExternalPublication = isExternalPublication;
        setCompositionRoot(mainHorizontalLayout);
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
        previewPanel.setParentPanel(this);


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
    
    private boolean isFilterCriteriaEmpty(List<FilterCriteria> filterCriteriaList){
        boolean isEmpty = true;
        for(FilterCriteria filter : filterCriteriaList){
            if(!filter.isEmpty())
                if(!filter.gotOnlyNulls())
                    isEmpty = false;
        }
        return isEmpty;
    }
    
 @Override
    public List<Publication> getPanelPublications()
    {
        for(Object id: resultTable.getItemIds())
        {
            allPublications.add((Publication) resultTable.getItem(id).getItemProperty(ResultTable.TABLE_PUBLICATION_COLUMN).getValue());
        }

        return allPublications;
    }
    
    @Override
    public Publication getCurrentPublication()
    {
        return previewPanel.getActivePublication();
    }
    
    @Override
    public void setBackup()
    {
        backupManager.setBackupPublications(getPanelPublications());
        backupManager.setPreviousPublication(getCurrentPublication());
        backupManager.setBackupScreen(this);
    }
    
    @Override
    public Table getPublicationTable()
    {
        return resultTable;
    }
    
    @Override
    public boolean isExternalPublication()
    {
        return isExternalPublication;
    }
    
    @Override
    public void setIsExternalPublication(boolean externalPublication)
    {
        isExternalPublication = externalPublication;
    }

    private void initSearchScreenPanel() {
//        setMargin(true);
//        setSpacing(true);
        setSizeFull();

        mainHorizontalLayout = initMainHorizontalLayout();

//        addComponent(menuBar);
//        addComponent(mainHorizontalLayout);
        
//        setExpandRatio(menuBar, 0);
//        setExpandRatio(mainHorizontalLayout, 1);
    }
}
