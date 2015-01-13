package com.publicationmetasearchengine.gui.homescreen;

import com.publicationmetasearchengine.dao.publications.exceptions.PublicationWithNoAuthorException;
import com.publicationmetasearchengine.dao.publications.exceptions.RelationAlreadyExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.RelationDoesNotExistException;
import com.publicationmetasearchengine.data.Author;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.management.authormanagement.AuthorManager;
import com.publicationmetasearchengine.management.publicationmanagement.PublicationManager;
import com.publicationmetasearchengine.utils.DateUtils;
import com.publicationmetasearchengine.utils.PMSEConstants;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
public class PreviewPanel extends PMSEPanel implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PreviewPanel.class);

    private static final String MARK_TO_READ_CAPTION = "Mark To Read";
    private static final String MARK_AS_READ_CAPTION = "Mark as Read";
    private static final String SHOW_OTHER_PUBLICATION_OF_AUTHOR = "Show more of this author";

    @Autowired
    private PublicationManager publicationManager;
    @Autowired
    private AuthorManager authorManager;
    
    private HomeScreenPanel homePanel;
    private final Label titleLbl = new Label();
    private final Label authorsLbl = new Label();
    private final Label publicationSourceLabel = new Label();
    private final Link doiLink = new Link("", null);
    private final Link pdfLink = new Link("", null);
    private final Label summaryLbl = new Label();
    private final Panel summaryPanel = new Panel();
    private final PMSEButton toReadBtn = new PMSEButton();
    private final PMSEButton showMoreBtn = new PMSEButton();

    private Publication activePublication;
    private User activeUser;
    
    private final Button.ClickListener showOtherPublicationListener = new Button.ClickListener() {
        private static final long serialVersionUID = 1L;
        
        @Override
        public void buttonClick(Button.ClickEvent event) {
            List<Publication> publications = publicationManager.getPublicationOfAuthor(activePublication.getMainAuthor());
            LOGGER.debug(String.format("Found %d publications of author", publications.size()));
            for (Publication publication : publications){
                LOGGER.debug("Found publications: "+ publication.getTitle());
                homePanel.filterPublicationByAuthorOfSelected(activePublication.getMainAuthor());
        }
            
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    
    
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

    public PreviewPanel(String caption) {
        super(caption);
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        vl.setSpacing(true);


        vl.addComponent(titleLbl);
        vl.addComponent(authorsLbl);
        vl.addComponent(publicationSourceLabel);
        vl.addComponent(doiLink);
        vl.addComponent(pdfLink);
        vl.addComponent(summaryPanel);
        vl.addComponent(toReadBtn);
        vl.addComponent(showMoreBtn);
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
    
    public void setHomePanel(HomeScreenPanel panel){
        this.homePanel = panel;
    }

    private void initializeToReadBtn() {
        toReadBtn.removeListener(markAsReadListener);
        toReadBtn.removeListener(markToReadListener);
        if (activeUser == null)
            return;
        if (publicationManager.isUserPublication(activeUser, activePublication)) {
            toReadBtn.setCaption(MARK_AS_READ_CAPTION);
            toReadBtn.addListener(markAsReadListener);
        }
        else {
            toReadBtn.setCaption(MARK_TO_READ_CAPTION);
            toReadBtn.addListener(markToReadListener);
        }
    }

    public void setContent(Publication publication) {
        activePublication = publication;
        activeUser = (User)getApplication().getUser();

        ArrayList<Author> publicationAuthors = null;
        try {
            publicationAuthors = authorManager.getPublicationAuthors(publication);
        } catch (PublicationWithNoAuthorException ex) {
            LOGGER.error(ex);
        }
        titleLbl.setValue(publication.getTitle());
        String authorsString = publicationAuthors!=null?concatAuthors(publicationAuthors): null;
        authorsLbl.setValue(authorsString!=null?"by: " + authorsString:"");
        if (publication.getSourceTitle() == null)
            publicationSourceLabel.setValue("Published: "+DateUtils.formatDateOnly(publication.getPublicationDate()));
        else
            publicationSourceLabel.setValue(String.format("Published: %s in %s, %s%s/%s",
                    DateUtils.formatDateOnly(publication.getPublicationDate()),
                    publication.getSourceTitle(),
                    publication.getSourceVolume()!=null?publication.getSourceVolume():"",
                    publication.getSourceIssue()!=null?"("+publication.getSourceIssue()+")":"",
                    DateUtils.formatYearOnly(publication.getPublicationDate())));

        final String doiString = publication.getDoi();
        setLink(doiLink, doiString, String.format("https://www.google.pl/search?q=\"%s\"", doiString));
        if (publication.getSourceDB().getShortName().equals(PMSEConstants.ARXIV_SHORT_NAME)&&doiString==null) {
            String arxivePageString = "arxiv.org/abs/" + publication.getArticleId();
            setLink(doiLink, arxivePageString, "http://"+arxivePageString);
        }
        if (publication.getPdfLink() != null )
            setLink(pdfLink, "PDF", publication.getPdfLink());
        else
            pdfLink.setCaption("");

        summaryLbl.setValue(publication.getSummary());

        initializeToReadBtn();
        showMoreBtn.setCaption(SHOW_OTHER_PUBLICATION_OF_AUTHOR);
        showMoreBtn.addListener(showOtherPublicationListener);
    }

    private void setLink(Link link, String caption, String resourceLink) {
        link.setCaption(caption);
        link.setResource(new ExternalResource(resourceLink));
    }

    private String concatAuthors(List<Author> authors) {
        StringBuilder sb = new StringBuilder(authors.get(0).getName());
        for(int i = 1; i < authors.size(); ++i)
            sb.append(", ").append(authors.get(i).getName());
        return sb.toString();
    }

    public void additionalMarkAsReadAction(){

    }

    public void additionalMarkToReadAction(){

    }
}
