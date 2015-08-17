/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.utils.impactfactor;

import com.publicationmetasearchengine.dao.impactfactor.ImpactFactorDAO;
import com.publicationmetasearchengine.dao.impactfactor.exceptions.JournalAlreadyExistException;
import com.publicationmetasearchengine.data.Journal;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author Kamciak
 */
@Configurable(preConstruction = true)
public class ImpactFactorDownloader {
    private static final Logger LOGGER = Logger.getLogger(ImpactFactorDownloader.class);
    private final String impactFactorUrl = "http://www.citefactor.org/journal-impact-factor-list-2014_%s.html";
    @Autowired
    ImpactFactorDAO impactFactorDao;
    
    public void downloadImpactFactor()
    {
        List<String> queries = getQueriesToDownload();
        ArrayList<Journal> allJournals = new ArrayList<Journal>();
        for(String query : queries)
        {
            LOGGER.debug("Downloading: " + query);
            
            try {
                ImpactFactorParser impactFactorParser = new ImpactFactorParser(downloadHTML(query));
                allJournals.addAll(impactFactorParser.parse());
            } catch (IOException ex) {
                LOGGER.error(ex);
            }
        }
        LOGGER.debug("Inserting to db. Size of: " + allJournals.size());
        for (Journal journal : allJournals) {
            LOGGER.debug("Inserting: " + journal);
            insertJournalToDb(journal);
        }
    }
    
    private void insertJournalToDb(Journal journal)
    {
        try {
            impactFactorDao.insertImpactFactor(journal);
        } catch (JournalAlreadyExistException ex) {
            LOGGER.debug(ex);
        }
    }
    
    private List<String> getQueriesToDownload(){
        ArrayList<String> queries = new ArrayList<String>();
        queries.add(String.format(impactFactorUrl, "0-A"));
        
            for(int c = 'B'; c<='Z'; ++c)
            {
                queries.add(String.format(impactFactorUrl, (char)c));
            }
        
        return queries;
    }
    
    
        private String downloadHTML(String htmlLink) throws IOException {
        return Jsoup.connect(htmlLink).timeout(120 * 1000).ignoreContentType(true).get().html();
    }
}
