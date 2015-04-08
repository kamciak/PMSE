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
import com.publicationmetasearchengine.utils.Notificator;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.ContentModelContainer;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
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
    private HorizontalLayout mainHorizontalLayout;
    boolean isPreviewVisible = false;

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
                new ConfirmWindow(getApplication(), "Question", "Do you want to remove marked publications?") {

                    @Override
                    public void yesButtonClick() {
                        final User user = (User) getApplication().getUser();
                        publicationManager.removeUserPublications(user);
                        toReadTable.clear();
                        Notificator.showNotification(getApplication(), "Info", "All marked publications have been removed.", Notificator.NotificationType.HUMANIZED);
                    }
                };
            }
        });

        toReadPanelLayout = new VerticalLayout();
        toReadPanelLayout.setMargin(true);
        toReadPanelLayout.setSpacing(true);
        toReadPanelLayout.addComponent(toReadTable);
        toReadPanelLayout.addComponent(downloadAll);
        toReadPanelLayout.addComponent(cleanList);
        toReadPanelLayout.setComponentAlignment(downloadAll, Alignment.TOP_RIGHT);
        toReadPanelLayout.setComponentAlignment(cleanList, Alignment.TOP_RIGHT);
        toReadPanel.setContent(toReadPanelLayout);
    }


    private void loadUsersPublications(User user) {
        toReadTable.addPublications(publicationManager.getUserPublications(user));
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
}
