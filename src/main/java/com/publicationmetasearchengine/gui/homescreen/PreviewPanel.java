package com.publicationmetasearchengine.gui.homescreen;

import com.publicationmetasearchengine.dao.publications.exceptions.PublicationWithNoAuthorException;
import com.publicationmetasearchengine.dao.publications.exceptions.RelationAlreadyExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.RelationDoesNotExistException;
import com.publicationmetasearchengine.data.Author;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.gui.ScreenPanel;
import com.publicationmetasearchengine.gui.mainmenu.MainMenuBarAuthorizedUser;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.management.authormanagement.AuthorManager;
import com.publicationmetasearchengine.management.publicationmanagement.PublicationManager;
import com.publicationmetasearchengine.services.ServiceJobProvider;
import com.publicationmetasearchengine.services.croneservice.CroneService;
import com.publicationmetasearchengine.services.datacollectorservice.arxiv.ArxivAuthorCollector;
import com.publicationmetasearchengine.utils.DateUtils;
import com.publicationmetasearchengine.utils.Notificator;
import com.publicationmetasearchengine.utils.PMSEConstants;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.crypto.Cipher;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.dialogs.ConfirmDialog;

@Configurable(preConstruction = true)
public class PreviewPanel extends PMSEPanel implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PreviewPanel.class);
    private static final String MARK_TO_READ_CAPTION = "Mark To Read";
    private static final String MARK_AS_READ_CAPTION = "Mark as Read";
    @Autowired
    private PublicationManager publicationManager;
    @Autowired
    private AuthorManager authorManager;
    private ScreenPanel parentPanel;
    private final Label titleLbl = new Label();
    private final Label authorsLbl = new Label();
    private final Label publicationSourceLabel = new Label();
    private final Link doiLink = new Link("", null);
    private final Link pdfLink = new Link("", null);
    private final Label summaryLbl = new Label();
    private final Panel summaryPanel = new Panel();
    private final PMSEButton toReadBtn = new PMSEButton();
    private Publication activePublication;
    private User activeUser;
    private VerticalLayout vl = new VerticalLayout();
    private CssLayout cl = new CssLayout();
    private final String confirmationText = "Searching in external libraries can take up to few minutes. Do you want to continue?";
    
    public Publication getActivePublication() {
        return activePublication;
    }
    private final Button.ClickListener markAsReadListener = new Button.ClickListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void buttonClick(Button.ClickEvent event) {
            try {
                publicationManager.removeUserPublication(activeUser, activePublication);
                toReadBtn.setCaption(MARK_TO_READ_CAPTION);
                additionalMarkAsReadAction();
            } catch (RelationDoesNotExistException ex) {
                LOGGER.error(ex);
            }
        }
    };
    private final Button.ClickListener markToReadListener = new Button.ClickListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void buttonClick(Button.ClickEvent event) {
            try {
                publicationManager.insertUserPublication(activeUser, activePublication);
                toReadBtn.setCaption(MARK_AS_READ_CAPTION);
                additionalMarkToReadAction();
            } catch (RelationAlreadyExistException ex) {
                LOGGER.error(ex);
            }
        }
    };

    private void initCssLayout() {
        cl.setMargin(false);
        cl.setWidth("100%");
        cl.setVisible(true);
    }

    public PreviewPanel(String caption) {
        super(caption);

        vl.setSizeFull();
        vl.setMargin(true);
        vl.setSpacing(true);
        vl.addComponent(titleLbl);

        initCssLayout();
        vl.addComponent(cl);
        vl.addComponent(publicationSourceLabel);
        vl.addComponent(doiLink);
        vl.addComponent(pdfLink);
        vl.addComponent(summaryPanel);

        toReadBtn.setVisible(false);
        vl.addComponent(toReadBtn);
        vl.setExpandRatio(summaryPanel, 1);

        setContent(vl);

        titleLbl.setStyleName("boldTitle");
        VerticalLayout summaryPanelLayout = new VerticalLayout();
        summaryPanelLayout.addComponent(summaryLbl);
        summaryPanelLayout.setSizeUndefined();
        summaryPanelLayout.setWidth("100%");
        summaryPanel.setContent(summaryPanelLayout);
        summaryPanel.setSizeFull();
        summaryPanel.setStyleName("borderless");


    }

    public void initializeActiveUser() {
        try {
            activeUser = (User) getApplication().getUser();
        } catch (NullPointerException e) {
            LOGGER.debug("Unauthorized user");
        }
    }

    public void showToReadBtn() {
        toReadBtn.setVisible(true);
    }

    public void hideToReadBtn() {
        toReadBtn.setVisible(false);
    }

    public void setParentPanel(ScreenPanel panel) {
        this.parentPanel = panel;
    }

    public List<Publication> findPublicationsBySelectedAuthor(String authorName) {
        return publicationManager.getPublicationOfAuthor(authorName);
    }

    private void initializeToReadBtn() {
        showToReadBtn();
        toReadBtn.removeListener(markAsReadListener);
        toReadBtn.removeListener(markToReadListener);
        if (activeUser == null) {
            hideToReadBtn();
            return;
        }
        if (publicationManager.isUserPublication(activeUser, activePublication)) {
            toReadBtn.setCaption(MARK_AS_READ_CAPTION);
            toReadBtn.addListener(markAsReadListener);
        } else {
            toReadBtn.setCaption(MARK_TO_READ_CAPTION);
            toReadBtn.addListener(markToReadListener);
        }
    }

    public void setContentForAuthorPublications(Publication publication)
    {
        List<Author> publicationAuthors = null;
        try{    
            publicationAuthors = publication.getAuthors();
        } catch (PublicationWithNoAuthorException ex) {
            LOGGER.error(ex);
        }
        prepareContent(publication, publicationAuthors);
    }
    
    public void setContent(Publication publication) {
        List<Author> publicationAuthors = null;
        try {
            publicationAuthors = authorManager.getPublicationAuthors(publication);

        } catch (PublicationWithNoAuthorException ex) {
            LOGGER.error(ex);
        }

        prepareContent(publication, publicationAuthors);
    }
    
    private void prepareContent(Publication publication, List<Author> publicationAuthors) {
        initializeActiveUser();
        initAuthorButtons(publicationAuthors);
        setPublicationSourceLabel(publication);
        setDoiLink(publication);
        setPdfLink(publication);

        activePublication = publication;
        titleLbl.setValue(publication.getTitle());
        summaryLbl.setValue(publication.getSummary());

        initializeToReadBtn();
    }
    
    private void setPublicationSourceLabel(Publication publication) {
        if (publication.getSourceTitle() == null && publication.getJournalRef() == null) {
            publicationSourceLabel.setValue("Published: " + DateUtils.formatDateOnly(publication.getPublicationDate()));
        } else {
            publicationSourceLabel.setValue(String.format("Published: %s in %s, %s%s/%s",
                    DateUtils.formatDateOnly(publication.getPublicationDate()),
                    publication.getSourceTitle() != null? publication.getSourceTitle() : publication.getJournalRef(),
                    publication.getSourceVolume() != null ? publication.getSourceVolume() : "",
                    publication.getSourceIssue() != null ? "(" + publication.getSourceIssue() + ")" : "",
                    DateUtils.formatYearOnly(publication.getPublicationDate())));
        }
    }
    
    private void setDoiLink(Publication publication) {
        final String doiString = publication.getDoi();
        setLink(doiLink, doiString, String.format("https://www.google.pl/search?q=\"%s\"", doiString));
        if (publication.getSourceDB().getShortName().equals(PMSEConstants.ARXIV_SHORT_NAME) && doiString == null) {
            String arxivePageString = "arxiv.org/abs/" + publication.getArticleId();
            setLink(doiLink, arxivePageString, "http://" + arxivePageString);
        }
    }
    
    private void setPdfLink(Publication publication) {
        if (publication.getPdfLink() != null) {
            setLink(pdfLink, "PDF", publication.getPdfLink());
        } else {
            pdfLink.setCaption("");
        }
    }

    private void setLink(Link link, String caption, String resourceLink) {
        link.setCaption(caption);
        link.setResource(new ExternalResource(resourceLink));
    }

    /**
     *
     * @param authors List of authors
     * @return String of authors of publication (ex. M. Li, A. Barald, C. Winx)
     */
    private String concatAuthors(List<Author> authors) {
        StringBuilder sb = new StringBuilder(authors.get(0).getName());
        for (int i = 1; i < authors.size(); ++i) {
            sb.append(", ").append(authors.get(i).getName());
        }
        return sb.toString();
    }

    private void initAuthorButtons(List<Author> publicationAuthors) {
        cl.removeAllComponents();
        authorsLbl.setValue(publicationAuthors != null ? "by: " : "");
        cl.addComponent(authorsLbl);

        for (Author author : publicationAuthors) {
            PMSEButton button = new PMSEButton(author.getName());
            PMSEButton searchForAllAuthorsPublication = new PMSEButton("Find all of: "+author.getName());
            final String authorName = author.getName();

            button.addListener(new Button.ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(Button.ClickEvent event) {

                    if (parentPanel instanceof HomeScreenPanel) {
                        ((HomeScreenPanel) parentPanel).filterPublicationByAuthorOfSelected(authorName);
                    } else {
                        List<Publication> publications = findPublicationsBySelectedAuthor(authorName);
                        getApplication().getMainWindow().setContent(new HomeScreenPanel(new MainMenuBarAuthorizedUser(), publications));

                    }
                }
            });

            searchForAllAuthorsPublication.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    ConfirmDialog.show(getWindow(), confirmationText,
                            new ConfirmDialog.Listener() {
                        @Override
                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {
                                ArxivAuthorCollector authorCollector = new ArxivAuthorCollector(authorName);
                                authorCollector.downloadAuthorPublications();

                                if (parentPanel instanceof HomeScreenPanel) {
                                    ((HomeScreenPanel) parentPanel).setBackupPublication();
                                    ((HomeScreenPanel) parentPanel).enableBackButon();
                                    ((HomeScreenPanel) parentPanel).addAuthorPublicationsToTable(authorCollector.getPublication());
                                } else {
                                    getApplication().getMainWindow().setContent(new HomeScreenPanel(new MainMenuBarAuthorizedUser(), authorCollector.getPublication()));
                                }
                            }
                        }
                    });
                }
            });
            cl.addComponent(button);
            cl.addComponent(searchForAllAuthorsPublication);
            button.setStyleName("link");
        }
    }

    public void additionalMarkAsReadAction() {
        toReadBtn.removeListener(markAsReadListener);
        toReadBtn.addListener(markToReadListener);
    }

    public void additionalMarkToReadAction() {
        toReadBtn.removeListener(markToReadListener);
        toReadBtn.addListener(markAsReadListener);
    } 
}
