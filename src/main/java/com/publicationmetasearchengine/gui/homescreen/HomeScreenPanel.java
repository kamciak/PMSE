package com.publicationmetasearchengine.gui.homescreen;

import com.publicationmetasearchengine.dao.sourcedbs.SourceDbDAO;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.data.SourceDB;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.gui.PublicationScreenPanel;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.gui.searchscreen.SearchScreenPanel;
import com.publicationmetasearchengine.gui.toreadscreen.ToReadScreenPanel;
import com.publicationmetasearchengine.management.backupmanagement.BackupManager;
import com.publicationmetasearchengine.management.publicationmanagement.PublicationManager;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
public class HomeScreenPanel extends VerticalLayout implements PublicationScreenPanel {
    private static final long serialVersionUID = 1L;
    private final MenuBar menuBar;
    private final PMSEPanel recentPanel = new PMSEPanel("Recent publications");
    HorizontalLayout searchLayout = new HorizontalLayout();
    private final PreviewPanel previewPanel = new PreviewPanel("Content");
    private final Map<String, Date> dateMap = new LinkedHashMap<String, Date>();
    private final PMSEButton goBackBtn = new PMSEButton("Go back");
    private final PublicationTable publicationTable = new PublicationTable();
    private HorizontalLayout mainHorizontalLayout;
    boolean isPreviewVisible = false;
    private static final Logger LOGGER = Logger.getLogger(PreviewPanel.class);
    {
        DateTime now = new DateTime();
        dateMap.put("Day", now.minusDays(1).toDate());
        dateMap.put("Week", now.minusWeeks(1).toDate());
        dateMap.put("Month", now.minusMonths(1).toDate());
        dateMap.put("Half Year", now.minusMonths(6).toDate());
    }
    private boolean isExternalPublication = false;
    @Autowired
    private SourceDbDAO sourceDbDAO;
    @Autowired
    private PublicationManager publicationManager;
    @Autowired
    private BackupManager backupManager;
    private final NativeSelect sourceDBCB = new NativeSelect("Source");
    private final NativeSelect dateCB = new NativeSelect("Period");
    private final PMSEButton refreshBtn = new PMSEButton("Refresh");
    private final KeywordFilter keywordFilter = new KeywordFilter("Title keywords") {
        private static final long serialVersionUID = 1L;

        @Override
        public void onFilterBtnClick() {
            publicationTable.filterByTitleKeywords(keywordFilter.getKeywords());
        }
    };

    public HomeScreenPanel(MenuBar menuBar) {
        super();
        this.menuBar = menuBar;
        initHomeScreenPanel();
        goBackBtn.setEnabled(false);
    }

    public HomeScreenPanel(MenuBar menuBar, List<Publication> publications, boolean isExternal) {
        super();
        this.menuBar = menuBar;
        isExternalPublication = isExternal;
        initHomeScreenPanel();
        goBackBtn.setEnabled(true);
        goBackBtn.removeListener(goBackBtnListener);
        publicationTable.cleanAndAddPublications(publications);
        publicationTable.setSelectable(true);

        goBackBtn.addListener(goBackBtnToReadScreenPanelListener);
        searchLayout.setEnabled(false);
    }
    
    private void initHomeScreenPanel() {
        setMargin(true);
        setSpacing(true);
        setSizeFull();

        mainHorizontalLayout = initMainHorizontalLayout();

        addComponent(menuBar);
        addComponent(mainHorizontalLayout);
        setExpandRatio(menuBar, 0);
        setExpandRatio(mainHorizontalLayout, 1);

        goBackBtn.addListener(goBackBtnListener);
        setHomePanelForPreviewPanel();
        backupManager.setIsExternalPublication(isExternalPublication);
    }

    private void setHomePanelForPreviewPanel() {
        previewPanel.setParentPanel(this);
    }

    public void addPublicationsToTable(List<Publication> publications) {
        publicationTable.cleanAndAddPublications(publications);
        searchLayout.setEnabled(false);
    }

    public void setProperPublicationsTableListener()
    {
        removeGoBackBtnToReadScreenPanelListener();
        goBackBtn.addListener(goBackBtnListener);
        if(isExternalPublication)
            setAuthorPublicationTableChangeListener();
        else
            setPublicationTableChangeListener();
    }
    
    public void filterPublicationByAuthorOfSelected(String authorName) {
        //setBackupPublications();
        List<Publication> publications = publicationManager.getPublicationOfAuthor(authorName);
        publicationTable.cleanAndAddPublications(publications);
        searchLayout.setEnabled(false);
    }

    private HorizontalLayout initMainHorizontalLayout() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setMargin(true);
        hl.setSpacing(true);
        hl.setSizeFull();

        initRecentPanelContent();
        initPreviewPanelContent();

        hl.addComponent(recentPanel);
        hl.setExpandRatio(recentPanel, 3);

        return hl;
    }

    private void initRecentPanelContent() {
        recentPanel.setSizeFull();
        //HorizontalLayout searchLayout = new HorizontalLayout();
        searchLayout.setSpacing(true);
        searchLayout.setSizeFull();
        searchLayout.addComponent(sourceDBCB);
        searchLayout.addComponent(dateCB);
        searchLayout.addComponent(refreshBtn);
        refreshBtn.addListener(refreshBtnListener);
        searchLayout.setComponentAlignment(refreshBtn, Alignment.BOTTOM_LEFT);
        
        searchLayout.addComponent(keywordFilter);
        searchLayout.setExpandRatio(keywordFilter, 1);
        searchLayout.setComponentAlignment(keywordFilter, Alignment.MIDDLE_RIGHT);
        

        publicationTable.setSizeFull();
        publicationTable.setSelectable(true);
        publicationTable.setImmediate(true);
        //publicationTable.addListener(publicationTableChangeListener);
        setProperPublicationsTableListener();
        dateCB.setNullSelectionAllowed(false);
        dateCB.setImmediate(true);
        for (Map.Entry<String, Date> entry : dateMap.entrySet()) {
            dateCB.addItem(entry.getValue());
            dateCB.setItemCaption(entry.getValue(), entry.getKey());
        }
        dateCB.select(dateMap.get("Day"));
        dateCB.addListener(cbValueChangeListener);
        
        List<SourceDB> allSourceDBS = sourceDbDAO.getAllSourceDBS();
        for (SourceDB sourceDB : allSourceDBS) {
            sourceDBCB.addItem(sourceDB);
            sourceDBCB.setItemCaption(sourceDB, sourceDB.getFullName());
        }
        sourceDBCB.setImmediate(true);
        sourceDBCB.setNullSelectionAllowed(false);
        sourceDBCB.addListener(cbValueChangeListener);
        sourceDBCB.select(allSourceDBS.get(0));
        
        HorizontalLayout bottomLayout = new HorizontalLayout();
        bottomLayout.setSpacing(true);
        bottomLayout.setSizeFull();
        bottomLayout.addComponent(goBackBtn);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");
        mainLayout.addComponent(searchLayout);
        mainLayout.addComponent(publicationTable);
        mainLayout.addComponent(bottomLayout);
        recentPanel.setContent(mainLayout);
    }

    private void initPreviewPanelContent() {
        previewPanel.setSizeFull();
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

    public void changePreviePanelVisibility() {
        setPreviewPanelVisibility(!isPreviewVisible);
    }
  
    @Override
    public List<Publication> getPanelPublications()
    {
        return publicationTable.getAllPublication();
    }
    
    @Override
    public Publication getCurrentPublication()
    {
        return previewPanel.getActivePublication();
    }
    
    @Override
    public void setBackup()
    {
        backupManager.setBackupScreen(this);
        enableBackButon();
    }
    
    @Override
    public Table getPublicationTable()
    {
        return publicationTable;
    }
    /*
    @Override
    public void setBackupPublications(List<Publication> publications){
        enableBackButon();
        if (previewPanel.isVisible()) {
            backupManager.setPreviousPublication(previewPanel.getActivePublication());
            //recentPublication = previewPanel.getActivePublication();
        }
        //backupPublications = new ArrayList<Publication>(publicationTable.getAllPublication());
        backupManager.setBackupPublications(publications);
        setPreviewPanelVisibility(false); 
    }
*/
    
    private void setBackupPublications() {
        enableBackButon();
        backupManager.setIsExternalPublication(isExternalPublication);
        if (previewPanel.isVisible()) {
            backupManager.setPreviousPublication(previewPanel.getActivePublication());
            //recentPublication = previewPanel.getActivePublication();
        }
        //backupPublications = new ArrayList<Publication>(publicationTable.getAllPublication());
        backupManager.setBackupPublications(getPanelPublications());
        
    }
    /*
    public List<Publication> getBackupPublications() {
        return backupPublications;
    }*/

    private void enableBackButon() {
        goBackBtn.setEnabled(true);
    }

    
    private void setPublicationTableChangeListener() {
        removeAllPublicationTableChangeListeners();
        publicationTable.addListener(publicationTableChangeListener);
    }
    
    private void setAuthorPublicationTableChangeListener() {
        removeAllPublicationTableChangeListeners();
        publicationTable.addListener(authorPublicationTableChangeListener);
    }
    
    public void removeAllPublicationTableChangeListeners(){
        publicationTable.removeListener(publicationTableChangeListener);
        publicationTable.removeListener(authorPublicationTableChangeListener);
    }

    /*
     *  LISTENERS
     *              */
    private Property.ValueChangeListener publicationTableChangeListener = new Property.ValueChangeListener() {
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
            Publication currentPublication = (Publication) publicationTable.getItem(id).getItemProperty(PublicationTable.TABLE_PUBLICATION_COLUMN).getValue();
            previewPanel.setContent(currentPublication);
        }
    };
    
    private Property.ValueChangeListener authorPublicationTableChangeListener = new Property.ValueChangeListener() {
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
            Publication currentPublication = (Publication) publicationTable.getItem(id).getItemProperty(PublicationTable.TABLE_PUBLICATION_COLUMN).getValue();
            previewPanel.setContentForAuthorPublications(currentPublication);
        }
    };
    
    private Property.ValueChangeListener cbValueChangeListener = new Property.ValueChangeListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void valueChange(Property.ValueChangeEvent event) {
            SourceDB sourceDB = (SourceDB) sourceDBCB.getValue();
            Date date = (Date) dateCB.getValue();
            if (sourceDB != null) {
                publicationTable.clear();
                final List<Publication> publications = publicationManager.getPublicationsBySourceDBAndDate(sourceDB, date);
                if (!publications.isEmpty()) {
                    if (!publicationTable.isSelectable()) {
                        publicationTable.setSelectable(true);
                    }
                    publicationTable.addPublications(publications);
                } else {
                    publicationTable.setSelectable(false);
                    publicationTable.addMockPublication(new Publication(null, null, null, null, "No publication found", null, null, null, null, null, null, null, null, null, null));
                }
            }
        }
    };


    private final Button.ClickListener goBackBtnListener = new Button.ClickListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void buttonClick(Button.ClickEvent event) {
            boolean isExternalPublicationTmp = backupManager.isExternalPublication();
            backupManager.setIsExternalPublication(isExternalPublication);
            setIsExternalPublication(isExternalPublicationTmp);

            publicationTable.cleanAndAddPublications(backupManager.getBackupPublications());

            setPreviewPanelVisibility(true);
            goBackBtn.setEnabled(false);
            if (isExternalPublication()) {
                setAuthorPublicationTableChangeListener();
                previewPanel.setContentForAuthorPublications(backupManager.getPreviousPublication());
            } else {
                setPublicationTableChangeListener();
                previewPanel.setContent(backupManager.getPreviousPublication());
            }
        }
    };
    
    private final Button.ClickListener goBackBtnToReadScreenPanelListener = new Button.ClickListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void buttonClick(Button.ClickEvent event) {
            //boolean isExternalPublicationTmp = backupManager.isExternalPublication();
            backupManager.setIsExternalPublication(isExternalPublication);
            PublicationScreenPanel previousPanel = backupManager.getBackupScreen();
            if(previousPanel instanceof ToReadScreenPanel)
                getApplication().getMainWindow().setContent(((User) getApplication().getUser()).getScreenPanel(new ToReadScreenPanel(false)));
            else if(previousPanel instanceof SearchScreenPanel)
                getApplication().getMainWindow().setContent(((User) getApplication().getUser()).getScreenPanel(new SearchScreenPanel(false)));
        }
    };
 
    private final Button.ClickListener refreshBtnListener = new Button.ClickListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void buttonClick(Button.ClickEvent event) {
            setBackupPublications();
            isExternalPublication = false;
            setProperPublicationsTableListener();
            SourceDB sourceDB = (SourceDB) sourceDBCB.getValue();
            Date date = (Date) dateCB.getValue();
            if (sourceDB != null) {
                publicationTable.clear();
                final List<Publication> publications = publicationManager.getPublicationsBySourceDBAndDate(sourceDB, date);
                if (!publications.isEmpty()) {
                    if (!publicationTable.isSelectable()) {
                        publicationTable.setSelectable(true);
                    }
                    publicationTable.addPublications(publications);
                } else {
                    publicationTable.setSelectable(false);
                    publicationTable.addMockPublication(new Publication(null, null, null, null, "No publication found", null, null, null, null, null, null, null, null, null, null));
                }
            }
        }
    };
    
    @Override
    public void setIsExternalPublication(boolean externalPublication)
    {
        isExternalPublication = externalPublication;
    }
    
    @Override
    public boolean isExternalPublication()
    {
        return isExternalPublication;
    }

    private void removeGoBackBtnToReadScreenPanelListener()
    {
        goBackBtn.removeListener(goBackBtnToReadScreenPanelListener);
    }

}
