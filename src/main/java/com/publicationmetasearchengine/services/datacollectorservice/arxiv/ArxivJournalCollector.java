/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.services.datacollectorservice.arxiv;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.publicationmetasearchengine.dao.impactfactor.ImpactFactorDAO;
import com.publicationmetasearchengine.dao.sourcedbs.SourceDbDAO;
import com.publicationmetasearchengine.dao.sourcedbs.exceptions.SourceDbDoesNotExistException;
import com.publicationmetasearchengine.data.Author;
import com.publicationmetasearchengine.data.Journal;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.services.datacollectorservice.arxiv.parser.ArxivParser;
import com.publicationmetasearchengine.services.datacollectorservice.arxiv.parser.RawEntry;
import com.publicationmetasearchengine.services.datacollectorservice.arxiv.querybuilder.QueryBuilder;
import com.publicationmetasearchengine.utils.JournalComparator;
import com.publicationmetasearchengine.utils.PMSEConstants;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author Kamciak
 */
@Configurable(preConstruction = true)
public class ArxivJournalCollector implements Serializable {

    private static final int fetchPackageSize = 100;
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ArxivDataCollector.class);
    private List<Publication> publicationList = new ArrayList<Publication>();
    private static int tmp = 1;
    @Autowired
    private SourceDbDAO sourceDbDAO;
    @Autowired
    ImpactFactorDAO impactFactorDao;
    private List<String> titleKeys;
    private List<String> abstractKeys;
    private ComboCondition.Op titleKeysInnerOperator;
    private ComboCondition.Op outerOperator;
    private ComboCondition.Op abstractKeysInnerOperator;
    private HashMap<String, ArrayList<Publication>> resultMap = new HashMap<String, ArrayList<Publication>>();
    private HashMap<String, List<Journal>> journalsHashMapStartsAt = new HashMap<String, List<Journal>>();

    public ArxivJournalCollector(List<String> titleKeys,
            ComboCondition.Op titleKeysInnerOperator,
            ComboCondition.Op outerOperator,
            List<String> abstractKeys,
            ComboCondition.Op abstractKeysInnerOperator) {
        this.titleKeys = titleKeys;
        this.abstractKeys = abstractKeys;
        this.outerOperator = outerOperator;
        this.titleKeysInnerOperator = titleKeysInnerOperator;
        this.abstractKeysInnerOperator = abstractKeysInnerOperator;
    }
    
    public List<Publication> getPublications() {
        return publicationList;
    }
    
    public HashMap<String, ArrayList<Publication>> getJournalPublications() {
        return resultMap;
    }

    public void downloadJournalPublications() {
        Stack<RawEntry> entryStack = new Stack<RawEntry>();
        try {
            for (int position = 0;; position += fetchPackageSize) {
                ArxivParser arxivParser = new ArxivParser(downloadHTML((new QueryBuilder(titleKeys, titleKeysInnerOperator, outerOperator, abstractKeys, abstractKeysInnerOperator, position, fetchPackageSize, false)).buildForJournal()));
                List<RawEntry> allEntries = arxivParser.getAllEntries();
                for (RawEntry rawEntry : allEntries) {
                    entryStack.add(rawEntry);
                }
                if (allEntries.isEmpty()) {
                    break;
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        
        while (!entryStack.isEmpty()) {
            addToList(entryStack.pop());
        }
        LOGGER.info("Fetching of journal ended...");
    }
    
    private void addToList(RawEntry record) {
        try {
            //TMP - identyfikator nieistotny na ten czas, podczas wrzucania do bazy bedzie podmienany, wymyslisc inny sposob, moze dodatkowy konsruktor?
            String authorName = "";
            if (!record.getAuthors().isEmpty()) {
                authorName = record.getAuthors().get(0);
            }
            
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
            
            publication.setSourceDbId(sourceDbDAO.getSourceIdByShortName(PMSEConstants.ARXIV_SHORT_NAME));
            //ZNOWU TROCHE BEZ SENSU, ALE AUTORZY POTRZEBNI DO PREVIEW, BO NIE POBIERANI BEZPOSREDNIO Z PUBLIKACJI
            List<Author> authorList = new ArrayList<Author>();
            for (String author : record.getAuthors()) {
                authorList.add(new Author(tmp, author));
            }
            publication.setAuthors(authorList);
            publicationList.add(publication);
            addJournal(publication);
            
        } catch (SourceDbDoesNotExistException ex) {
            LOGGER.error(ex);
        }
    }
    
    private String downloadHTML(String htmlLink) throws IOException {
        return Jsoup.connect(htmlLink).timeout(120 * 1000).ignoreContentType(true).get().html();
    }

    private void addJournal(Publication publication) {
        String journalRef = publication.getJournalRef();
        String journalTitle = null;
        
        if (journalRef != null) {
            journalRef = journalRef.split(",", 0)[0];
            char firstLetterOfJournal = journalRef.charAt(0);
            if (!journalsHashMapStartsAt.keySet().contains((String.valueOf(firstLetterOfJournal)))) {
                journalsHashMapStartsAt.put(String.valueOf(firstLetterOfJournal), impactFactorDao.getJournalsStartAt(firstLetterOfJournal));
            }
            List<Journal> journalsStartAt = journalsHashMapStartsAt.get(String.valueOf(firstLetterOfJournal));
            
            Collections.sort(journalsStartAt, new JournalComparator());
            journalRef = journalRef.replace(":", "");
            journalTitle = journalRef;
            for (int i = 0; i < journalsStartAt.size(); ++i) {
                if (Pattern.compile(Pattern.quote(journalsStartAt.get(i).getTitle()), Pattern.CASE_INSENSITIVE).matcher(journalRef).find()) {
                    journalTitle = journalsStartAt.get(i).getTitle();
                    break;
                }
            }
            publication.setJournalTitle(journalTitle);
        }
        if (resultMap.containsKey(journalTitle)) {
            ((ArrayList<Publication>) resultMap.get(journalTitle)).add(publication);
        } else {
            ArrayList<Publication> publications = new ArrayList<Publication>();
            publications.add(publication);
            resultMap.put(journalTitle, publications);
        }
    }
}