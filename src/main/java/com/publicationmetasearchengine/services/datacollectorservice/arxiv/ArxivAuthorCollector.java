/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.services.datacollectorservice.arxiv;

import com.publicationmetasearchengine.dao.sourcedbs.SourceDbDAO;
import com.publicationmetasearchengine.dao.sourcedbs.exceptions.SourceDbDoesNotExistException;
import com.publicationmetasearchengine.data.Author;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.services.datacollectorservice.arxiv.parser.ArxivParser;
import com.publicationmetasearchengine.services.datacollectorservice.arxiv.parser.RawEntry;
import com.publicationmetasearchengine.services.datacollectorservice.arxiv.querybuilder.QueryBuilder;
import com.publicationmetasearchengine.utils.PMSEConstants;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author Kamciak
 */
@Configurable(preConstruction = true)
public class ArxivAuthorCollector implements Serializable {
    private static final int fetchPackageSize = 100;
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ArxivDataCollector.class);
    private List<Publication> publicationList = new ArrayList<Publication>();
    private static int tmp = 1;
    @Autowired
    private SourceDbDAO sourceDbDAO;

    private String authorName;
    public ArxivAuthorCollector(String author)
    {
        this.authorName = author;
    }
     
    private String prepareAuthorNameForSearch(String authorName) {
        List<String> authorData = new LinkedList<String>(Arrays.asList(authorName.replace("-", "_").split(" ")));
        String authorSurename = authorData.remove(0);
        char firstLetterOfAuthorName = authorData.remove(0).charAt(0);
        return authorSurename + "_" + firstLetterOfAuthorName;
    }


    public void downloadAuthorPublications() {
        String authorData = prepareAuthorNameForSearch(authorName);
        fetchAuthor(authorData);
    }

    public List<Publication> getPublication() {
        return publicationList;
    }

    private void fetchAuthor(String authorData) {
        Stack<RawEntry> entryStack = new Stack<RawEntry>();
        try {
            for (int position = 0;; position += fetchPackageSize) {
                ArxivParser arxivParser = new ArxivParser(downloadHTML((new QueryBuilder(authorData, position, fetchPackageSize, false)).buildForAuthor()));
                List<RawEntry> authorEtnries = arxivParser.getAuthorEntries();
                for (RawEntry rawEntry : authorEtnries) {
                    entryStack.add(rawEntry);
                }
                if (authorEtnries.isEmpty()) {
                    break;
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
        }

        while (!entryStack.isEmpty()) {
            addToList(entryStack.pop());
        }


        LOGGER.info(String.format("Fetching of author: %s ended...", authorData));
    }

    private void addToList(RawEntry record) {
        try {
            //TMP - identyfikator nieistotny na ten czas, podczas wrzucania do bazy bedzie podmienany, wymyslisc inny sposob, moze dodatkowy konsruktor?
            Publication publication = new Publication(++tmp,
                    sourceDbDAO.getSourceDBById(sourceDbDAO.getSourceIdByShortName(PMSEConstants.ARXIV_SHORT_NAME)),
                    record.getId(),
                    authorName,
                    record.getTitle(),
                    record.getSummary(),
                    record.getDoi(),
                    record.getJournalRef(),
                    null,
                    null,
                    null,
                    null,
                    record.getPublicationDate(),
                    record.getPdfLink(),
                    null);
            
            //ZNOWU TROCHE BEZ SENSU, ALE AUTORZY POTRZEBNI DO PREVIEW, BO NIE POBIERANI BEZPOSREDNIO Z PUBLIKACJI
            List<Author> authorList = new ArrayList<Author>();
            for(String author : record.getAuthors()){
                authorList.add(new Author(tmp, author));
            }
            publication.setAuthors(authorList);
            publicationList.add(publication);
            
        } catch (SourceDbDoesNotExistException ex) {
            java.util.logging.Logger.getLogger(ArxivAuthorCollector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String downloadHTML(String htmlLink) throws IOException {
        return Jsoup.connect(htmlLink).timeout(120 * 1000).ignoreContentType(true).get().html();
    }
}
