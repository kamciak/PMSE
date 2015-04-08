package com.publicationmetasearchengine.gui.homescreen;

import com.publicationmetasearchengine.dao.sourcedbs.SourceDbDAO;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.data.SourceDB;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.gui.ScreenPanel;
import com.publicationmetasearchengine.gui.mainmenu.MainMenuBarAuthorizedUser;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.gui.toreadscreen.ToReadScreenPanel;
import com.publicationmetasearchengine.management.publicationmanagement.PublicationManager;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
public class HomeScreenPanel extends VerticalLayout implements ScreenPanel {

    private static final long serialVersionUID = 1L;
    private final MenuBar menuBar;
    private final PMSEPanel recentPanel = new PMSEPanel("Recent publications");
    private final PreviewPanel previewPanel = new PreviewPanel("Content");
    private final Map<String, Date> dateMap = new LinkedHashMap<String, Date>();
    private final PMSEButton goBackBtn = new PMSEButton("Go back");
    private List<Publication> backupPublications = new ArrayList<Publication>();
    private Publication recentPublication;
    private final PublicationTable publicationTable = new PublicationTable();
    private HorizontalLayout mainHorizontalLayout;
    boolean isPreviewVisible = false;

    {
        DateTime now = new DateTime();
        dateMap.put("Day", now.minusDays(1).toDate());
        dateMap.put("Week", now.minusWeeks(1).toDate());
        dateMap.put("Month", now.minusMonths(1).toDate());
        dateMap.put("Half Year", now.minusMonths(6).toDate());
    }
    @Autowired
    private PublicationManager publicationManager;
    @Autowired
    private SourceDbDAO sourceDbDAO;
    private final NativeSelect sourceDBCB = new NativeSelect("Source");
    private final NativeSelect dateCB = new NativeSelect("Period");
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
        this.goBackBtn.setEnabled(false);
    }

    public HomeScreenPanel() {
        super();
        menuBar = new MainMenuBarAuthorizedUser();
        initHomeScreenPanel();
        goBackBtn.setEnabled(false);
    }

    public HomeScreenPanel(MenuBar menuBar, List<Publication> publications) {
        super();
        this.menuBar = menuBar;
        initHomeScreenPanel();
        publicationTable.cleanAndAddPublications(publications);
        goBackBtn.setEnabled(true);
        goBackBtn.removeListener(goBackBtnListener);
        goBackBtn.addListener(goBackBtnToReadScreenPanelListener);
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
    }

    private void setHomePanelForPreviewPanel() {
        previewPanel.setParentPanel(this);
    }

    public void addPublicationsToTable(List<Publication> publications) {
        //setBackupPublications();
        publicationTable.cleanAndAddPublications(publications);
        setPublicationTableChangeListener();
    }

    public void addAuthorPublicationsToTable(List<Publication> publications) {
        //setBackupPublications();
        publicationTable.cleanAndAddPublications(publications);
        setAuthorPublicationTableChangeListener();
    }

    public void filterPublicationByAuthorOfSelected(String authorName) {
        setBackupPublications();
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
        HorizontalLayout searchLayout = new HorizontalLayout();
        searchLayout.setSpacing(true);
        searchLayout.setSizeFull();
        searchLayout.addComponent(sourceDBCB);
        searchLayout.addComponent(dateCB);
        searchLayout.addComponent(keywordFilter);
        searchLayout.setExpandRatio(keywordFilter, 1);
        searchLayout.setComponentAlignment(keywordFilter, Alignment.MIDDLE_RIGHT);

        publicationTable.setSizeFull();
        publicationTable.setSelectable(true);
        publicationTable.setImmediate(true);
        publicationTable.addListener(publicationTableChangeListener);

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

    public void setBackupPublications() {
        enableBackButon();
        if (previewPanel.isVisible()) {
            recentPublication = previewPanel.getActivePublication();
        }
        backupPublications = new ArrayList<Publication>(publicationTable.getAllPublication());
        setPreviewPanelVisibility(false);
    }
    
    public List<Publication> getBackupPublications() {
        return backupPublications;
    }

    public void enableBackButon() {
        goBackBtn.setEnabled(true);
    }

    private void setPublicationTableChangeListener() {
        publicationTable.removeListener(authorPublicationTableChangeListener);
        publicationTable.addListener(publicationTableChangeListener);
    }
    
    private void setAuthorPublicationTableChangeListener() {
        publicationTable.removeListener(publicationTableChangeListener);
        publicationTable.addListener(authorPublicationTableChangeListener);
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
            addPublicationsToTable(backupPublications);
            previewPanel.setContent(recentPublication);
            setPreviewPanelVisibility(true);
            goBackBtn.setEnabled(false);            //pytanie czy trzeba wylaczyc? mozna by odpalic, i skad tam i spowrotem
        }
    };
    
    private final Button.ClickListener goBackBtnToReadScreenPanelListener = new Button.ClickListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void buttonClick(Button.ClickEvent event) {
            getApplication().getMainWindow().setContent(((User) getApplication().getUser()).getScreenPanel(new ToReadScreenPanel()));
        }
    };
}
