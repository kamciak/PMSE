package com.publicationmetasearchengine.gui.homescreen;

import com.publicationmetasearchengine.PMSEAppLevelWindow;
import com.publicationmetasearchengine.PMSENavigableApplication;
import com.publicationmetasearchengine.dao.sourcedbs.SourceDbDAO;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.data.SourceDB;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.gui.PublicationScreenPanel;
import com.publicationmetasearchengine.gui.dialog.CheckboxConfirmDialog;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.gui.searchscreen.SearchScreenPanel;
import com.publicationmetasearchengine.gui.toreadscreen.ToReadScreenPanel;
import com.publicationmetasearchengine.management.backupmanagement.BackupManager;
import com.publicationmetasearchengine.management.publicationmanagement.PublicationManager;
import com.publicationmetasearchengine.services.datacollectorservice.arxiv.ArxivAuthorCollector;
import com.publicationmetasearchengine.services.datacollectorservice.bwn.BWNAuthorCollector;
import com.publicationmetasearchengine.services.datacollectorservice.wok.WoKAuthorCollector;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.navigator7.Navigator;
import org.vaadin.navigator7.Page;
import org.vaadin.navigator7.ParamChangeListener;
import org.vaadin.navigator7.uri.Param;

@Page(uriName = "HomeScreenPanel")
@SuppressWarnings("serial")
@Configurable(preConstruction = true)
public class HomeScreenPanel extends CustomComponent implements PublicationScreenPanel, ParamChangeListener {

    @Autowired
    private SourceDbDAO sourceDbDAO;
    @Autowired
    private PublicationManager publicationManager;
    @Autowired
    private BackupManager backupManager;
    private static final long serialVersionUID = 1L;
    private final PMSEPanel recentPanel = new PMSEPanel("Recent publications");
    HorizontalLayout searchLayout = new HorizontalLayout();
    private final PreviewPanel previewPanel = new PreviewPanel("Content");
    private final Map<String, Date> dateMap = new LinkedHashMap<String, Date>();
    private final PMSEButton goBackBtn = new PMSEButton("Go back");
    private final PMSEButton showRecentPublicationBtn = new PMSEButton("Show recent publications");
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
    private final NativeSelect sourceDBCB = new NativeSelect("Source");
    private final NativeSelect dateCB = new NativeSelect("Period");
    final String confirmationText = "Searching in external libraries can take up to few minutes. Do you want to continue?";
    private final KeywordFilter keywordFilter = new KeywordFilter("Title keywords") {
        private static final long serialVersionUID = 1L;

        @Override
        public void onFilterBtnClick() {
            publicationTable.filterByTitleKeywords(keywordFilter.getKeywords());
        }
    };

    public HomeScreenPanel() {
        super();
        initHomeScreenPanel();
        goBackBtn.setEnabled(false);
        initMenuBar();
    }

    private void initHomeScreenPanel() {
        mainHorizontalLayout = initMainHorizontalLayout();
        mainHorizontalLayout.setSizeFull();
        setCompositionRoot(mainHorizontalLayout);

        setHomePanelForPreviewPanel();

        goBackBtn.addListener(goBackBtnListener);
        backupManager.setIsExternalPublication(isExternalPublication);
        publicationTable.setSelectable(true);
    }

    private void setHomePanelForPreviewPanel() {
        previewPanel.setParentPanel(this);
    }

    public void addPublicationsToTable(List<Publication> publications) {
        publicationTable.cleanAndAddPublications(publications);
    }

    public void setProperPublicationsTableListener() {
        goBackBtn.addListener(goBackBtnListener);
        if (isExternalPublication) {
            setAuthorPublicationTableChangeListener();
        } else {
            setPublicationTableChangeListener();
        }
    }

    public void filterPublicationByAuthorOfSelected(String authorName) {
        List<Publication> publications = publicationManager.getPublicationOfAuthor(authorName);
        publicationTable.cleanAndAddPublications(publications);
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
        searchLayout.setSpacing(true);
        searchLayout.setSizeFull();
        searchLayout.addComponent(sourceDBCB);
        searchLayout.addComponent(dateCB);


        searchLayout.addComponent(keywordFilter);
        searchLayout.setExpandRatio(keywordFilter, 1);
        searchLayout.setComponentAlignment(keywordFilter, Alignment.MIDDLE_RIGHT);

        publicationTable.setSizeFull();
        publicationTable.setImmediate(true);
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

        bottomLayout.addComponent(goBackBtn);
        bottomLayout.addComponent(showRecentPublicationBtn);
        showRecentPublicationBtn.addListener(refreshBtnListener);
        bottomLayout.setComponentAlignment(goBackBtn, Alignment.MIDDLE_LEFT);
        goBackBtn.setSizeUndefined();
        showRecentPublicationBtn.setSizeUndefined();
        bottomLayout.setComponentAlignment(showRecentPublicationBtn, Alignment.MIDDLE_LEFT);

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
    public List<Publication> getPanelPublications() {
        return publicationTable.getAllPublication();
    }

    @Override
    public Publication getCurrentPublication() {
        return previewPanel.getActivePublication();
    }

    @Override
    public void setBackup() {
        backupManager.setBackupScreen(this);
        enableBackButon();
    }

    @Override
    public Table getPublicationTable() {
        return publicationTable;
    }

    private void setBackupPublications() {
        enableBackButon();
        backupManager.setIsExternalPublication(isExternalPublication);
        if (previewPanel.isVisible()) {
            backupManager.setPreviousPublication(previewPanel.getActivePublication());
        }
        backupManager.setBackupPublications(getPanelPublications());

    }

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

    public void removeAllPublicationTableChangeListeners() {
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
            searchLayout.setEnabled(false);
            setPreviewPanelVisibility(true);
            goBackBtn.setEnabled(false);
            if (isExternalPublication()) {

                PMSENavigableApplication.getCurrentNavigableAppLevelWindow().getNavigator().setUriParams("#E");
                setAuthorPublicationTableChangeListener();
                previewPanel.setContentForAuthorPublications(backupManager.getPreviousPublication());
            } else {
                PMSENavigableApplication.getCurrentNavigableAppLevelWindow().getNavigator().setUriParams("#L");
                setPublicationTableChangeListener();
                previewPanel.setContent(backupManager.getPreviousPublication());
            }
        }
    };

    private final Button.ClickListener refreshBtnListener = new Button.ClickListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void buttonClick(Button.ClickEvent event) {
            searchLayout.setEnabled(true);
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
    public void setIsExternalPublication(boolean externalPublication) {
        isExternalPublication = externalPublication;
    }

    @Override
    public boolean isExternalPublication() {
        return isExternalPublication;
    }


    private void initMenuBar() {
        if (((PMSEAppLevelWindow) (PMSENavigableApplication.getCurrentNavigableAppLevelWindow())).getApplication().getUser() == null) {
            ((PMSEAppLevelWindow) (PMSENavigableApplication.getCurrentNavigableAppLevelWindow())).initUnauthorizedMenuBar();
        } else {
            ((PMSEAppLevelWindow) (PMSENavigableApplication.getCurrentNavigableAppLevelWindow())).initAuthorizedMenuBar();
        }
    }
    @Param(pos = 0)
    String authorName;

    @Override
    public void paramChanged(Navigator.NavigationEvent event) {
        if (authorName != null) {
            if (authorName.contains("E#")) {
                createBackup(true);
                handleSearchForExternalPublicationOfAuthor(authorName);

            } else {
                createBackup(false);
                handleSearchForLocalPublicationOfAuthor(authorName);
            }

            if (authorName.startsWith("DE#") || authorName.startsWith("DL#")) {
                goBackBtn.setVisible(false);
            }
            searchLayout.setEnabled(false);
            publicationTable.setSelectable(true);
        }
    }

    private void createBackup(boolean isExternal) {
        backupManager.setIsExternalPublication(isExternalPublication);
        backupManager.setBackupPublications(getPanelPublications());
        backupManager.setPreviousPublication(previewPanel.getActivePublication());

        setBackup();
        setIsExternalPublication(isExternal);
    }

    private void handleSearchForLocalPublicationOfAuthor(final String authorName) {
        String authorNameCut = authorName.replaceFirst("[ED]L#", "");
        filterPublicationByAuthorOfSelected(authorNameCut);
        setProperPublicationsTableListener();
    }

    private void handleSearchForExternalPublicationOfAuthor(final String authorName) {
        CheckboxConfirmDialog.show(getWindow(), confirmationText,
                new CheckboxConfirmDialog.Listener() {
            @Override
            public void onClose(CheckboxConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    String authorNameCut = authorName.replaceFirst("[ED]E#", "");
                    List<Publication> allPublications = new ArrayList<Publication>();
                    if (dialog.searchInArxiv()) {
                        allPublications.addAll(getArxivAuthorPublications(authorNameCut));
                    }
                    if (dialog.searchInBwn()) {
                        allPublications.addAll(getBWNAuthorPublications(authorNameCut));
                    }
                    if (dialog.searchInWoK()) {
                        allPublications.addAll(getWoKAuthorPublications(authorNameCut));
                    }

                    addPublicationsToTable(allPublications);
                    setProperPublicationsTableListener();
                }
            }
        });
    }

    private List<Publication> getArxivAuthorPublications(String authorName) {
        ArxivAuthorCollector arxivAuthorCollector = new ArxivAuthorCollector(authorName);
        arxivAuthorCollector.downloadAuthorPublications();
        return arxivAuthorCollector.getPublications();
    }

    private List<Publication> getBWNAuthorPublications(String authorName) {
        BWNAuthorCollector bwnAuthorCollector = new BWNAuthorCollector(authorName);
        bwnAuthorCollector.downloadAuthorPublications();
        return bwnAuthorCollector.getPublications();
    }

    private List<Publication> getWoKAuthorPublications(String authorName) {
        WoKAuthorCollector wokAuthorCollector = new WoKAuthorCollector(authorName);
        wokAuthorCollector.downloadAuthorPublications();
        return wokAuthorCollector.getPublications();
    }
}
