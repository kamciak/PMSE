package com.publicationmetasearchengine.gui.toreadscreen;

import com.publicationmetasearchengine.PMSEAppLevelWindow;
import com.publicationmetasearchengine.PMSENavigableApplication;
import com.publicationmetasearchengine.dao.publications.exceptions.PublicationWithNoAuthorException;
import com.publicationmetasearchengine.dao.publications.exceptions.RelationDoesNotExistException;
import com.publicationmetasearchengine.gui.homescreen.*;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.gui.ConfirmWindow;
import com.publicationmetasearchengine.gui.PublicationScreenPanel;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.management.authormanagement.AuthorManager;
import com.publicationmetasearchengine.management.backupmanagement.BackupManager;
import com.publicationmetasearchengine.management.publicationmanagement.PublicationManager;
import com.publicationmetasearchengine.utils.BibTeXGenerator;
import com.publicationmetasearchengine.utils.BibliographyGenerator;
import com.publicationmetasearchengine.utils.Notificator;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
public class ToReadScreenPanel extends CustomComponent implements PublicationScreenPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ToReadScreenPanel.class);
    private boolean isExternalPublication = false;
    @Autowired
    private PublicationManager publicationManager;
    @Autowired
    private BackupManager backupManager;
    @Autowired
    private AuthorManager authorManager;
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
    private PMSEButton showBibliography = new PMSEButton("Show");
    private PMSEButton saveBibliography = new PMSEButton("Save");
    private HorizontalLayout mainHorizontalLayout;
    private boolean isAllSelected = false;
    private boolean isPreviewVisible = false;
    private List<String> bibtexStringList = new ArrayList<String>();
    private List<String> bibliographyStringList = new ArrayList<String>();
    private PMSEPanel bibliographyPanel = new PMSEPanel("Bibliography");
    private TextArea bibliographyTextArea = new TextArea();
    private final NativeSelect bibliographySelect = new NativeSelect();
    private final String BIBTEX_BIBLIOGRAPHY = "Bibtex";
    private final String PLAIN_BIBLIOGRAPHY = "Plain text";
    private final String HIDE_BIBLIOGRAPHY_LBL = "Hide";
    private final String SHOW_BIBLIOGRAPHY_LBL = "Show";
    private static final String BIBLIOGRAPHY_DOWNLOADER = "Bibliography downloader";
    private static final String SELECT_INFO = "Please select type of bibliography";

    public ToReadScreenPanel() {
        super();
        initToReadScreenPanel();
    }

    @Override
    public void attach() {
        if (((PMSEAppLevelWindow) (PMSENavigableApplication.getCurrentNavigableAppLevelWindow())).getApplication().getUser() == null) {
            PMSENavigableApplication.getCurrentNavigableAppLevelWindow().getNavigator().navigateTo(HomeScreenPanel.class);
        } else {
            super.attach();
            loadUsersPublications((User) getApplication().getUser());
        }
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

        markAll.setStyleName(ChameleonTheme.BUTTON_SMALL);
        initBibliographySelect();

        CssLayout managmentLayout = initManagmentLayout();
        toReadPanelLayout.addComponent(managmentLayout);

        toReadPanelLayout.addComponent(bibliographyPanel);
        toReadPanel.setContent(toReadPanelLayout);

        bibliographyPanel.setStyleName(ChameleonTheme.PANEL_BORDERLESS);
        bibliographyPanel.setVisible(false);
        setShowHideBibliographyLabel();
    }

    private void loadUsersPublications(User user) {
        toReadTable.addPublications(publicationManager.getUserPublications(user));
    }

    private CssLayout getStyledCssLayout() {
        CssLayout cl = new CssLayout();
        cl.setStyleName("my_css_layout");

        return cl;
    }

    private void initPreviewPanelContent() {
        previewPanel.setSizeFull();
        previewPanel.setParentPanel(this);
    }
    private Property.ValueChangeListener bibliographySelectListener = new Property.ValueChangeListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void valueChange(Property.ValueChangeEvent event) {
            if((String) bibliographySelect.getValue() != null)
            {
                setProperBibliography();
            } 
            else {
                bibliographyPanel.setVisible(false);
            }
            setShowHideBibliographyLabel();
        }
    };

    private void clearBibliographyTextArea() throws Property.ConversionException, Property.ReadOnlyException {
        bibliographyTextArea.setReadOnly(false);
        bibliographyTextArea.setValue("");
        bibliographyTextArea.setReadOnly(true);
    }

    private void setProperBibliography() throws Property.ConversionException, Property.ReadOnlyException {
        String selectedValue = (String) bibliographySelect.getValue();
        if (selectedValue == null) {
            clearBibliographyTextArea();
        } else if (selectedValue.equals(BIBTEX_BIBLIOGRAPHY)) {
            generateBibTeX();
        } else if (selectedValue.equals(PLAIN_BIBLIOGRAPHY)) {
            generateBibliography();
        }
        
         bibliographyPanel.setVisible(true);
        
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

    private void setShowHideBibliographyLabel() {
        if (bibliographyPanel.isVisible()) {
            showBibliography.setCaption(HIDE_BIBLIOGRAPHY_LBL);
        } else {
            showBibliography.setCaption(SHOW_BIBLIOGRAPHY_LBL);
        }
    }

    private void addListeners() {
        markAll.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (isAllSelected) {
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

                if (!isPreviewVisible) {
                    setPreviewPanelVisibility(true);
                }
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
                if (publicationDownloader.downloadPublications(allPublications)) {
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
                        for (Object selectedItem : selectedIds) {
                            toReadTable.removeItem(selectedItem);
                        }
                        toReadTable.removeSelectedItemsIds(selectedIds);
                        Notificator.showNotification(getApplication(), "Info", "All marked publications have been removed.", Notificator.NotificationType.HUMANIZED);
                    }
                };
            }
        });


        showBibliography.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                bibliographyPanel.setVisible(!(bibliographyPanel.isVisible()));
                if(bibliographyPanel.isVisible()){
                    setProperBibliography();
                }
                setShowHideBibliographyLabel();

            }
        });

        saveBibliography.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                BibliographyDownloader bibliographyDownloader = new BibliographyDownloader(getApplication(), (User) getApplication().getUser());
                String selectedValue = (String) bibliographySelect.getValue();

                if (selectedValue == null) {
                    Notificator.showNotification(getApplication(), BIBLIOGRAPHY_DOWNLOADER, SELECT_INFO, Notificator.NotificationType.HUMANIZED);
                } else if (selectedValue.equals(BIBTEX_BIBLIOGRAPHY)) {
                    if (bibtexStringList.isEmpty()) {
                        generateBibTeX();
                    }
                    bibliographyDownloader.downloadBibliography(bibtexStringList, ".bib");
                } else if (selectedValue.equals(PLAIN_BIBLIOGRAPHY)) {
                    if (bibliographyStringList.isEmpty()) {
                        generateBibliography();
                    }
                    bibliographyDownloader.downloadBibliography(bibliographyStringList, ".txt");
                }
            }
        });
    }

    private void setAuthors(List<Publication> publicationList) {
        for (Publication publication : publicationList) {
            try {
                publication.setAuthors(authorManager.getPublicationAuthors(publication));
            } catch (PublicationWithNoAuthorException ex) {
                LOGGER.debug(ex);
            }
        }
    }

    private void generateBibTeX() {
        bibliographyPanel.setCaption("BibTeX");
        bibtexStringList = BibTeXGenerator.generate(gerPublicationsForBibliography());
        setBibliographyPanelContent(bibtexStringList, 6);
    }

    private void generateBibliography() {
        bibliographyPanel.setCaption("Biliography");
        bibliographyStringList = BibliographyGenerator.generate(gerPublicationsForBibliography());
        setBibliographyPanelContent(bibliographyStringList, 3);
    }

    private List<Publication> gerPublicationsForBibliography() {
        List<Publication> publicationList = toReadTable.getSelectedPublications();
        setAuthors(publicationList);
        return publicationList;
    }

    private void setBibliographyPanelContent(List<String> stringList, int multiplier) {
        bibliographyPanel.removeAllComponents();
        String content = "";
        for (String string : stringList) {
            content += string;
        }
        initBibliographyTextArea(content, multiplier * stringList.size());
        bibliographyPanel.addComponent(bibliographyTextArea);
    }

    @Override
    public List<Publication> getPanelPublications() {
        return toReadTable.getPublications();
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
        return toReadTable;
    }

    @Override
    public boolean isExternalPublication() {
        return isExternalPublication;
    }

    @Override
    public void setIsExternalPublication(boolean externalPublication) {
        isExternalPublication = externalPublication;
    }

    private void initToReadScreenPanel() {
        setSizeFull();
        mainHorizontalLayout = initMainHorizontalLayout();
        setCompositionRoot(mainHorizontalLayout);
    }

    private void initBibliographyTextArea(String content, int size) throws Property.ConversionException, Property.ReadOnlyException {
        bibliographyTextArea.setWordwrap(true);
        bibliographyTextArea.setReadOnly(false);
        bibliographyTextArea.setValue(content);
        bibliographyTextArea.setReadOnly(true);
        bibliographyTextArea.setSizeFull();
        bibliographyTextArea.setRows(size);
        bibliographyTextArea.setReadOnly(true);
    }

    private CssLayout initTableManagmentLayout() {
        CssLayout tableManagmentLayout = getStyledCssLayout();

        tableManagmentLayout.addComponent(markAll);
        tableManagmentLayout.addComponent(downloadAll);
        tableManagmentLayout.addComponent(cleanList);
        tableManagmentLayout.addComponent(deleteSelected);
        tableManagmentLayout.setCaption("Table management");

        return tableManagmentLayout;
    }

    private CssLayout initBibliographyManagmentLayout() {
        CssLayout bibliographyManagmentLayout = getStyledCssLayout();

        bibliographyManagmentLayout.addComponent(bibliographySelect);
        bibliographyManagmentLayout.addComponent(saveBibliography);
        bibliographyManagmentLayout.addComponent(showBibliography);
        bibliographyManagmentLayout.setCaption("Bibliography management");

        return bibliographyManagmentLayout;
    }

    private void initBibliographySelect() throws UnsupportedOperationException {
        bibliographySelect.setNullSelectionItemId("Select type");
        bibliographySelect.setNullSelectionAllowed(true);
        bibliographySelect.setImmediate(true);
        bibliographySelect.addItem(BIBTEX_BIBLIOGRAPHY);
        bibliographySelect.addItem(PLAIN_BIBLIOGRAPHY);
        bibliographySelect.addListener(bibliographySelectListener);
    }

    private CssLayout initManagmentLayout() {
        CssLayout managmentLayout = new CssLayout();

        CssLayout tableManagmentLayout = initTableManagmentLayout();
        CssLayout bibliographyManagmentLayout = initBibliographyManagmentLayout();
        managmentLayout.addComponent(tableManagmentLayout);
        managmentLayout.addComponent(bibliographyManagmentLayout);

        return managmentLayout;
    }
}
