/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.gui.searchjournalscreen;

/**
 *
 * @author Kamciak
 */
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.publicationmetasearchengine.PMSEAppLevelWindow;
import com.publicationmetasearchengine.PMSENavigableApplication;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.data.filters.FilterCriteria;
import com.publicationmetasearchengine.gui.PublicationScreenPanel;
import com.publicationmetasearchengine.gui.homescreen.HomeScreenPanel;
import com.publicationmetasearchengine.gui.homescreen.PreviewPanel;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.gui.searchscreen.ResultTable;
import com.publicationmetasearchengine.management.backupmanagement.BackupManager;
import com.publicationmetasearchengine.management.publicationmanagement.PublicationManager;
import com.publicationmetasearchengine.services.datacollectorservice.arxiv.ArxivJournalCollector;
import com.publicationmetasearchengine.services.datacollectorservice.bwn.BWNJournalCollector;
import com.publicationmetasearchengine.services.datacollectorservice.wok.WoKDataCollector;
import com.publicationmetasearchengine.services.datacollectorservice.wok.WoKJournalCollector;
import com.publicationmetasearchengine.utils.Notificator;
import com.publicationmetasearchengine.utils.impactfactor.ImpactFactorMapper;
import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
public class SearchJournalScreenPanel extends CustomComponent implements PublicationScreenPanel {

    private static final Logger LOGGER = Logger.getLogger(SearchJournalScreenPanel.class);
    private static final long serialVersionUID = 1L;
    private boolean isExternalPublication = true;
    @Autowired
    PublicationManager publicationManager;
    @Autowired
    private BackupManager backupManager;
    private List<Publication> allPublications = new ArrayList<Publication>();
    HashMap<String, ArrayList<Publication>> journals = new HashMap<String, ArrayList<Publication>>();
    private FiltersJournalPanel filtersPanel = new FiltersJournalPanel("Filters") {
        private static final long serialVersionUID = 1L;

        @Override
        public void searchClick() {
            journals.clear();
            resultTable.clear();
            final List<FilterCriteria> filtersCriteria = getFiltersCriteria();
            if (!isFilterCriteriaEmpty(filtersCriteria)) {
                if (checkBoxArxiv.booleanValue()) {

                    ArxivJournalCollector ajc = new ArxivJournalCollector(filtersCriteria.get(0).getValues(),
                            filtersCriteria.get(0).getInnerOperator(),
                            filtersCriteria.get(0).getOuterOperator(),
                            filtersCriteria.get(1).getValues(),
                            filtersCriteria.get(1).getInnerOperator());

                    ajc.downloadJournalPublications();
                    HashMap<String, ArrayList<Publication>> journalPublications = ajc.getJournalPublications();
                    for(String key : journalPublications.keySet()){
                        addToHashMap(key, journalPublications.get(key));
                    }
                }
                if (checkBoxBwn.booleanValue()) {
                    BWNJournalCollector bwnjc = new BWNJournalCollector(filtersCriteria.get(0).getValues(), 
                                                                        filtersCriteria.get(0).getOuterOperator(),
                                                                        filtersCriteria.get(1).getValues());
                    
                    bwnjc.downloadJournalPublications();
                    HashMap<String, ArrayList<Publication>> journalPublications = bwnjc.getJournalPublications();
                    for(String key : journalPublications.keySet()){
                        addToHashMap(key, journalPublications.get(key));
                    }
                }

                if (checkBoxWoK.booleanValue()) {
                    WoKJournalCollector wjc = new WoKJournalCollector(filtersCriteria.get(0).getValues());
                    wjc.downloadJournalPublications();
                    HashMap<String, ArrayList<Publication>> journalPublications = wjc.getJournalPublications();
                    for (String key : journalPublications.keySet()) {
                        addToHashMap(key, journalPublications.get(key));
                    }
                }

                addJournalsTable();

            } else {
                Notificator.showNotification(getApplication(), "No filter criteria", "\nEmpty filter criteria. Please fill any of field.", Notificator.NotificationType.HUMANIZED);
            }
        }

        
        private void addToHashMap(String title, ArrayList<Publication> publications)
        {
            if(journals.containsKey(title)){
                ArrayList<Publication> allPublication = journals.get(title);
                allPublication.addAll(publications);
                journals.put(title, allPublication);
            } else
            {
                journals.put(title, publications);
            }
        }
        
        private void addJournalsTable() {
            ImpactFactorMapper ifMapper = new ImpactFactorMapper();
            for (String key : journals.keySet()) {
                resultTable.addJournal(key, journals.get(key).size(), ifMapper.getImpactFactorForJournal(key));
            }
        }
    };
    private PMSEPanel resultPanel = new PMSEPanel("Result");
    private ResultJournalTable resultTable = new ResultJournalTable();
    private PMSEPanel resultPublicationPanel = new PMSEPanel();
    private ResultTable resultPublicationTable = new ResultTable();
    private PreviewPanel previewPanel = new PreviewPanel("Content");
    private HorizontalLayout mainHorizontalLayout;
    boolean isPreviewVisible = false;
    boolean isResultPublicationPanelVisible = false;
    private VerticalLayout searchJournalLayout = new VerticalLayout();
    final CheckBox checkBoxArxiv = new CheckBox("Arxiv", true);
    final CheckBox checkBoxBwn = new CheckBox("BWN", true);
    final CheckBox checkBoxWoK = new CheckBox("WoK", true);

    public SearchJournalScreenPanel() {
        super();
        initSearchScreenPanel();
        searchJournalLayout.addComponent(mainHorizontalLayout);
        
        initResultPublicationPanel();
        initCheckBoxes();
        setCompositionRoot(searchJournalLayout);
    }
    
    private void initResultPublicationPanel()
    {
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        vl.setSpacing(true);
        
        resultPublicationTable.setSizeFull();
        resultPublicationTable.setSelectable(true);
        resultPublicationTable.setImmediate(true);
        vl.addComponent(resultPublicationTable);
        
        resultPublicationPanel.setVisible(isResultPublicationPanelVisible);
        resultPublicationPanel.setContent(vl);
        searchJournalLayout.addComponent(resultPublicationPanel);
    }
    
    @Override
    public void attach() {
        if (((PMSEAppLevelWindow) (PMSENavigableApplication.getCurrentNavigableAppLevelWindow())).getApplication().getUser() == null) {
            PMSENavigableApplication.getCurrentNavigableAppLevelWindow().getNavigator().navigateTo(HomeScreenPanel.class);
        } else {
            super.attach();
        }
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
        resultTable.setSelectable(true);
        resultTable.setImmediate(true);
        resultTable.addListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Object id = event.getProperty().getValue();
                LOGGER.debug("id: " + id);
                if (id == null) {
                    resultPublicationPanel.setVisible(false);
                    return;
                }         
                
                if(!resultPublicationPanel.isVisible())
                    resultPublicationPanel.setVisible(true);
                String journalTitle = (String)resultTable.getItem(id).getItemProperty(ResultJournalTable.TABLE_JOURNAL_COLUMN).getValue();
                resultPublicationPanel.setCaption("Publication from: " + journalTitle);
                allPublications = journals.get(journalTitle);
                LOGGER.debug("Rozmiar: " + allPublications.size());
                resultPublicationTable.removeAllItems();
                
                resultPublicationTable.addPublications(allPublications);
                
            }
        });
        
                resultPublicationTable.addListener(new Property.ValueChangeListener() {
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
                Publication publication = (Publication) resultPublicationTable.getItem(id).getItemProperty(ResultTable.TABLE_PUBLICATION_COLUMN).getValue();
                previewPanel.setContentForAuthorPublications(publication);
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

    private boolean isFilterCriteriaEmpty(List<FilterCriteria> filterCriteriaList) {
        boolean isEmpty = true;
        for (FilterCriteria filter : filterCriteriaList) {
            if (!filter.isEmpty()) {
                if (!filter.gotOnlyNulls()) {
                    isEmpty = false;
                }
            }
        }
        return isEmpty;
    }

    @Override
    public List<Publication> getPanelPublications() {
        for (Object id : resultTable.getItemIds()) {
            allPublications.add((Publication) resultTable.getItem(id).getItemProperty(ResultTable.TABLE_PUBLICATION_COLUMN).getValue());
        }

        return allPublications;
    }

    @Override
    public Publication getCurrentPublication() {
        return previewPanel.getActivePublication();
    }

    @Override
    public void setBackup() {
        backupManager.setBackupPublications(getPanelPublications());
        backupManager.setPreviousPublication(getCurrentPublication());
        backupManager.setBackupScreen(this);
    }

    @Override
    public Table getPublicationTable() {
        return resultTable;
    }

    @Override
    public boolean isExternalPublication() {
        return isExternalPublication;
    }

    @Override
    public void setIsExternalPublication(boolean externalPublication) {
        isExternalPublication = externalPublication;
    }

    private void initSearchScreenPanel() {
        setSizeFull();
        mainHorizontalLayout = initMainHorizontalLayout();
    }

    private void initCheckBoxes() {
        VerticalLayout checkBoxes = new VerticalLayout();
        checkBoxes.setSpacing(true);
        checkBoxes.setWidth("100%");
        
        checkBoxes.addComponent(checkBoxArxiv);
        checkBoxes.addComponent(checkBoxBwn);
        checkBoxes.addComponent(checkBoxWoK);
        filtersPanel.addComponent(checkBoxes);
    }
}
