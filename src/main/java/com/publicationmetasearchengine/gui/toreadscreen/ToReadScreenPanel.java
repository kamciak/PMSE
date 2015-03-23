package com.publicationmetasearchengine.gui.toreadscreen;

import com.publicationmetasearchengine.dao.publications.exceptions.RelationDoesNotExistException;
import com.publicationmetasearchengine.gui.homescreen.*;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.gui.ConfirmWindow;
import com.publicationmetasearchengine.gui.ScreenPanel;
import com.publicationmetasearchengine.gui.mainmenu.MainMenuBarAuthorizedUser;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.management.publicationmanagement.PublicationManager;
import com.publicationmetasearchengine.utils.BibTeXGenerator;
import com.publicationmetasearchengine.utils.Notificator;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
public class ToReadScreenPanel extends VerticalLayout implements ScreenPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ToReadScreenPanel.class);


    private final MainMenuBarAuthorizedUser menuBar = new MainMenuBarAuthorizedUser();

    @Autowired
    private PublicationManager publicationManager;

    private final PMSEPanel toReadPanel = new PMSEPanel("Publications marked to read");
    private final PreviewPanel previewPanel = new PreviewPanel("Content") {
        private static final long serialVersionUID = 1L;

        @Override
        public void additionalMarkAsReadAction() {
            toReadTable.removeItem(toReadTable.getValue());
        }

    };


    private ToReadTable toReadTable = new ToReadTable();
    private VerticalLayout toReadPanelLayout;
    private PMSEButton downloadAll = new PMSEButton("Download all PDFs");
    private PMSEButton cleanList = new PMSEButton("Clean list");
    private PMSEButton deleteSelected = new PMSEButton("Delete selected publications");
    private PMSEButton markAll = new PMSEButton("Select all");
    private PMSEButton generateBibTeX = new PMSEButton("Generate BibTeX");
    private PMSEButton showBibTeXPanel = new PMSEButton("Show/Hide BibTeX panel");
    private PMSEButton saveBibTeX = new PMSEButton("Save BibTeX");
    private HorizontalLayout mainHorizontalLayout;
    private boolean isAllSelected = false;
    private boolean isPreviewVisible = false;
    private List<String> bibtexStringList = new ArrayList<String>();
    private PMSEPanel bibtexPanel = new PMSEPanel();
    public ToReadScreenPanel(){
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

    @Override
    public void attach() {
        super.attach();
        loadUsersPublications((User)getApplication().getUser());
    }

    private HorizontalLayout initMainHorizontalLayout() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setMargin(true);
        hl.setSpacing(true);
        hl.setSizeFull();

        initToReadPanelContent();
        initPreviewPanelContent();

        hl.addComponent(toReadPanel);
        hl.setExpandRatio(toReadPanel, 3);

        return hl;
    }

    private void initToReadPanelContent() {
        toReadPanel.setSizeFull();
        toReadTable.setSizeFull();
        toReadTable.setSelectable(true);
        toReadTable.setImmediate(true);
        
        
        addListeners();
        
        toReadPanelLayout = new VerticalLayout();
        toReadPanelLayout.setMargin(true);
        toReadPanelLayout.setSpacing(true);
        toReadPanelLayout.addComponent(toReadTable);
        
        /*
         * Buttons
         */
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        markAll.setStyleName(ChameleonTheme.BUTTON_SMALL);
        hl.addComponent(markAll);
        hl.addComponent(downloadAll);
        hl.addComponent(cleanList);
        hl.addComponent(deleteSelected);
        hl.addComponent(generateBibTeX);
        hl.addComponent(showBibTeXPanel);
        hl.addComponent(saveBibTeX);
        toReadPanelLayout.addComponent(hl);
        toReadPanelLayout.addComponent(bibtexPanel);
        bibtexPanel.setSizeFull();
        bibtexPanel.setCaption("BibTeX");
        bibtexPanel.setStyleName(ChameleonTheme.PANEL_BORDERLESS);
        
        toReadPanel.setContent(toReadPanelLayout);
        bibtexPanel.setVisible(false);
    }

    private void loadUsersPublications(User user) {
        toReadTable.addPublications(publicationManager.getUserPublications(user));
    }

    private void initPreviewPanelContent() {
        previewPanel.setSizeFull();
        previewPanel.setParentPanel(this);
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

    private void addListeners() {
        markAll.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;
            @Override
            public void buttonClick(Button.ClickEvent event) {  
                if(isAllSelected){
                    toReadTable.unselectAll();
                    isAllSelected = false;
                } else {
                    toReadTable.selectAll();
                    isAllSelected = true;
                }
            }
        });
        
        toReadTable.addListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Object id = event.getProperty().getValue();
                if (id == null) {
                    setPreviewPanelVisibility(false);
                    return;
                }

                if (!isPreviewVisible)
                    setPreviewPanelVisibility(true);
                Publication publication = (Publication) toReadTable.getItem(id).getItemProperty(PublicationTable.TABLE_PUBLICATION_COLUMN).getValue();
                previewPanel.setContent(publication);
            }
        });
        
        
        downloadAll.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                downloadAll.setEnabled(false);
                getApplication().getMainWindow().executeJavaScript("vaadin.forceLayout()");
                final User user = (User) getApplication().getUser();
                final List<Publication> allPublications = toReadTable.getPublications();
                final PublicationDownloader publicationDownloader = new PublicationDownloader(getApplication(), user);
                if (publicationDownloader.downloadPublications(allPublications) ) {
                    final PMSEButton removeDownloadedBtn = new PMSEButton("Remove downloaded publications");
                    removeDownloadedBtn.addListener(new Button.ClickListener() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            for (Publication publication : publicationDownloader.getDowloadedPublications()) {
                                try {
                                    publicationManager.removeUserPublication(user, publication);
                                    toReadTable.clear();
                                    loadUsersPublications(user);
                                } catch (RelationDoesNotExistException ex) {
                                }
                                toReadPanelLayout.removeComponent(removeDownloadedBtn);
                            }
                        }
                    });
                    toReadPanelLayout.addComponent(removeDownloadedBtn, 1);
                    toReadPanelLayout.setComponentAlignment(removeDownloadedBtn, Alignment.MIDDLE_RIGHT);

                }
                downloadAll.setEnabled(true);
            }
        });
        cleanList.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                new ConfirmWindow(getApplication(), "Question", "Do you want to remove all publications?") {

                    @Override
                    public void yesButtonClick() {
                        final User user = (User) getApplication().getUser();
                        publicationManager.removeUserPublications(user);
                        toReadTable.clear();
                        Notificator.showNotification(getApplication(), "Info", "All publications have been removed.", Notificator.NotificationType.HUMANIZED);
                    }
                };
            }
        });
        
        deleteSelected.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                new ConfirmWindow(getApplication(), "Question", "Do you want to remove marked publications?") {

                    @Override
                    public void yesButtonClick() {
                        final User user = (User) getApplication().getUser();
                        Set<Object> selectedIds = toReadTable.getSelectedItemIds();
                        publicationManager.removeUserSelectedPublications(user, toReadTable.getSelectedPublications());
                        for(Object selectedItem : selectedIds){
                            toReadTable.removeItem(selectedItem);
                        }
                        toReadTable.removeSelectedItemsIds(selectedIds);
                        Notificator.showNotification(getApplication(), "Info", "All marked publications have been removed.", Notificator.NotificationType.HUMANIZED);
                    }
                };
            }
        });
        
        generateBibTeX.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                generateBibTeX();
                bibtexPanel.setVisible(true);
            }
        });
        showBibTeXPanel.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                bibtexPanel.setVisible(!(bibtexPanel.isVisible()));
            }
        });
        
        saveBibTeX.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;
            @Override
            public void buttonClick(Button.ClickEvent event) {
                BibTeXDownloader bibtexDownload = new BibTeXDownloader(getApplication(), (User)getApplication().getUser());
                if(bibtexStringList.isEmpty())
                    generateBibTeX();
                bibtexDownload.downloadBibTex(bibtexStringList);
            }
        });
    }
    
    private void generateBibTeX(){
        bibtexStringList = BibTeXGenerator.generate(toReadTable.getSelectedPublications());
        bibtexPanel.removeAllComponents();
        for (String b : bibtexStringList) {
            Label bibtexLabel = new Label(b, Label.CONTENT_PREFORMATTED);
            bibtexPanel.addComponent(bibtexLabel);
        }
    }
    
}
