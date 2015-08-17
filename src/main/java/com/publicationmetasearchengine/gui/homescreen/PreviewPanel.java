package com.publicationmetasearchengine.gui.homescreen;

import com.publicationmetasearchengine.dao.authors.exceptions.AuthorAlreadyExistException;
import com.publicationmetasearchengine.dao.authors.exceptions.AuthorDoesNotExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.PublicationAlreadyExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.PublicationDoesNotExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.PublicationWithNoAuthorException;
import com.publicationmetasearchengine.dao.publications.exceptions.RelationAlreadyExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.RelationDoesNotExistException;
import com.publicationmetasearchengine.dao.sourcetitles.SourceTitleDAO;
import com.publicationmetasearchengine.dao.sourcetitles.exceptions.SourceTitleAlreadyExists;
import com.publicationmetasearchengine.dao.sourcetitles.exceptions.SourceTitleDoesNotExists;
import com.publicationmetasearchengine.data.Author;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.gui.PublicationScreenPanel;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.gui.searchjournalscreen.SearchJournalScreenPanel;
import com.publicationmetasearchengine.management.authormanagement.AuthorManager;
import com.publicationmetasearchengine.management.publicationmanagement.PublicationManager;
import com.publicationmetasearchengine.utils.DateUtils;
import com.publicationmetasearchengine.utils.PMSEConstants;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.navigator7.uri.ParamPageResource;

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
    @Autowired
    private SourceTitleDAO sourceTitleDAO;
    private PublicationScreenPanel parentPanel;
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

    private void initCssLayout() {
        cl.setMargin(false);
        cl.setWidth("100%");
        cl.setVisible(true);
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

    public void setParentPanel(PublicationScreenPanel panel) {
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

    public void setContentForAuthorPublications(Publication publication) {
        List<Author> publicationAuthors = null;
        try {
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
                    publication.getSourceTitle() != null ? publication.getSourceTitle() : publication.getJournalRef(),
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
        link.setTargetName("_blank");
    }

    private void initAuthorButtons(List<Author> publicationAuthors) {
        cl.removeAllComponents();
        cl.addComponent(authorsLbl);
        if (publicationAuthors != null) {
            authorsLbl.setValue("by: ");
            for (Author author : publicationAuthors) {
                Link button;
                Link searchForAllAuthorsPublication = new Link();
                if (!(parentPanel instanceof HomeScreenPanel)) {
                    button = new Link(author.getName(), new ParamPageResource(HomeScreenPanel.class, "DL#" + author.getName())); //$1 - Enable/Disable goBackBtn, //$2 - Local/External search
                    searchForAllAuthorsPublication.setResource(new ParamPageResource(HomeScreenPanel.class, "DE#" + author.getName()));
                } else {
                    button = new Link(author.getName(), new ParamPageResource(HomeScreenPanel.class, "EL#" + author.getName()));
                    searchForAllAuthorsPublication.setResource(new ParamPageResource(HomeScreenPanel.class, "EE#" + author.getName()));
                }

                button.setStyleName("v-button v-button-link link");

                searchForAllAuthorsPublication.setStyleName("v-button v-button-link link");
                String basepath = getApplication().getContext().getBaseDirectory().getAbsolutePath();
                FileResource resource = new FileResource(new File(basepath + "/WEB-INF/images/search-icon-md.png"), getApplication());
                searchForAllAuthorsPublication.setIcon(resource);

                cl.addComponent(button);
                cl.addComponent(searchForAllAuthorsPublication);
            }
        } else {
            authorsLbl.setValue("");
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
                if ((parentPanel instanceof HomeScreenPanel || parentPanel instanceof SearchJournalScreenPanel) && parentPanel.isExternalPublication()) {
                    Integer publicationId = insertActivePublicationIntoDB();
                    if (publicationId != null) {
                        try {
                            publicationManager.insertUserPublication(activeUser, publicationManager.getPublicationById(publicationId));
                        } catch (PublicationDoesNotExistException ex) {
                            LOGGER.warn(String.format("Publication doesn't exist. Id: [%s]", publicationId));
                        }
                    }
                } else {
                    publicationManager.insertUserPublication(activeUser, activePublication);
                }
                toReadBtn.setCaption(MARK_AS_READ_CAPTION);
                additionalMarkToReadAction();
            } catch (RelationAlreadyExistException ex) {
                LOGGER.error(ex);
            }
        }
    };

    private Integer insertActivePublicationIntoDB() {
        Integer publicationId = null;
        try {
            LOGGER.debug("Inserting " + activePublication.getTitle());
            List<Integer> authorIds = insertPublicationAuthorsToDb();
            Integer sourceTitleId = insertSourceTitleToDb();
            publicationId = insertPublicationToDb(authorIds, sourceTitleId);
        } catch (PublicationWithNoAuthorException ex) {
            java.util.logging.Logger.getLogger(PreviewPanel.class.getName()).log(Level.SEVERE, null, ex);
            LOGGER.warn(String.format("Publication [Arxiv - %s (%s)] withour authors", activePublication.getArticleId(), activePublication.getTitle()));
        }
        return publicationId;
    }

    private Integer insertSourceTitleToDb() {
        Integer sourceTitleId = null;
        if (activePublication.getSourceTitle() != null) {
            String sourceTitle = activePublication.getSourceTitle();
            try {
                sourceTitleId = sourceTitleDAO.addTitle(sourceTitle);
            } catch (SourceTitleAlreadyExists ex) {
                try {
                    sourceTitleId = sourceTitleDAO.getTitleIdByTitle(sourceTitle);
                } catch (SourceTitleDoesNotExists ex1) {
                    LOGGER.fatal("Should not occure", ex1);
                }
            }

        }
        return sourceTitleId;
    }

    private List<Integer> insertPublicationAuthorsToDb() throws PublicationWithNoAuthorException {
        List<Integer> authorIds = new ArrayList<Integer>();
        for (Author author : activePublication.getAuthors()) {
            try {
                authorIds.add(authorManager.addNewAuthor(author));
            } catch (AuthorAlreadyExistException ex) {
                try {
                    authorIds.add(authorManager.getAuthorIdByName(author.getName()));
                } catch (AuthorDoesNotExistException ex1) {
                    if (author.getName().length() > PMSEConstants.AUTHOR_MAX_NAME_LENGHT) {
                        LOGGER.warn(String.format("Author name [%s] is to long", author.getName()));
                    } else {
                        LOGGER.fatal("Should not occure !!", ex1);
                    }
                }
            }
        }
        return authorIds;
    }

    private int insertPublicationToDb(List<Integer> authorIds, Integer sourceTitleId) {
        Integer publicationId = null;
        try {
            publicationId = publicationManager.insertPublication(
                    activePublication.getSourceDbId(),
                    activePublication.getArticleId(),
                    authorIds.get(0),
                    activePublication.getTitle(),
                    activePublication.getSummary(),
                    activePublication.getDoi(),
                    activePublication.getJournalRef(),
                    sourceTitleId,
                    null,
                    null,
                    null,
                    activePublication.getPublicationDate(),
                    activePublication.getPdfLink());

            authorManager.setPublicationAuthorsIds(publicationId, authorIds);
            return publicationId;
        } catch (PublicationAlreadyExistException ex) {
            LOGGER.warn(String.format("Publication [Arxiv - %s (%s)] already exists", activePublication.getId(), activePublication.getTitle()));
            try {
                publicationId = publicationManager.getPublicationByArticleId(activePublication.getArticleId()).getId();
            } catch (PublicationDoesNotExistException ex1) {
                LOGGER.warn(String.format("Publication [Arxiv, articleId - %s (%s)] doesn't exist. Shouldn't occure.", activePublication.getArticleId(), activePublication.getTitle()));
            }
            return publicationId;
        }
    }
}