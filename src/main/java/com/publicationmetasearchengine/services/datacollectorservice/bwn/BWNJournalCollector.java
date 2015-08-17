/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.services.datacollectorservice.bwn;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.publicationmetasearchengine.dao.impactfactor.ImpactFactorDAO;
import com.publicationmetasearchengine.dao.properties.PropertiesManager;
import com.publicationmetasearchengine.dao.sourcedbs.SourceDbDAO;
import com.publicationmetasearchengine.dao.sourcedbs.exceptions.SourceDbDoesNotExistException;
import com.publicationmetasearchengine.data.Author;
import com.publicationmetasearchengine.data.Journal;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.services.datacollectorservice.bwn.parser.ContentTableParser;
import com.publicationmetasearchengine.services.datacollectorservice.bwn.parser.MainTableParser;
import com.publicationmetasearchengine.utils.JournalComparator;
import com.publicationmetasearchengine.utils.PMSEConstants;
import com.thoughtworks.xstream.InitializationException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
public class BWNJournalCollector {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(BWNAuthorCollector.class);

    private static final String SEARCH_JOURNAL_TEMPLATE = "http://vls2.icm.edu.pl/cgi-bin/"
            + "search.pl?SearchTemplate=search_form.expert&GetSearchResults=Submit+Query&"
            + "search_field=%s&fields=%s&AdvBooleanJoiner=%s&"
            + "search_field2=%s&fields2=%s&Database=elsevier_1990&Database=springer_1990&"
            + "Category=all_categories&ArticleType=Article&Language=&"
            + "daterange=yearsince&drsince=2000&drpast=none&fromyear=none&"
            + "toyear=none&Max=750&Start=1&Order=SORT+DATE+DESC&GetSearchResults=Submit+Query";

    @Autowired
    private SourceDbDAO sourceDbDAO;
    @Autowired
    ImpactFactorDAO impactFactorDao;
    private String SOCKSproxyHostPort;
    private int mainTableDownloadTimeout;
    private int contentDownloadTimeout;
    private Queue<String> contentLinksQueue = new LinkedList<String>();
    private Queue<String> contentsQueue = new LinkedList<String>();
    private Integer sourceDBId = null;
    private static final String LINK_PREFIX = "http://vls2.icm.edu.pl/";
    private List<Publication> publicationList = new ArrayList<Publication>();
    private static int tmp = 1;
    private int contentThreadPoolSize;
        
    private String titleKeys;
    private String abstractKeys;
    private ComboCondition.Op outerOperator;
    private HashMap<String, ArrayList<Publication>> resultMap = new HashMap<String, ArrayList<Publication>>();
    private HashMap<String, List<Journal>> journalsHashMapStartsAt = new HashMap<String, List<Journal>>();

    public BWNJournalCollector(List<String> titleKeys,
            ComboCondition.Op outerOperator,
            List<String> abstractKeys) {
        this.titleKeys = prepareString(titleKeys);
        this.abstractKeys = prepareString(abstractKeys);
        this.outerOperator = outerOperator;
    }
    
    private String operatorToString(ComboCondition.Op operator) {
        if (operator == ComboCondition.Op.AND) {
            return "AND";
        } else {
            return "OR";
        }
    }
    
        private String prepareString(List<String> stringList) {
        if(stringList.isEmpty())
            return "";
        

        String result = "";
        for (int j = 0; j < stringList.size(); ++j) {
            String[] filtrList = stringList.get(j).split(" ");
            for (int i = 0; i < filtrList.length; ++i) {
                result += filtrList[i];
                if (filtrList.length == i + 1) {
                    break;
                } else {
                    result += "+";
                }
            }
            if (j + 1 == stringList.size()) {
                break;
            } else {
                result += "+";
            }
        }

        return result;
    }
    

    private void initialize() {
        final String collectorPrefix = "datacollector.BWN.";
        final PropertiesManager pm = PropertiesManager.getInstance();
        SOCKSproxyHostPort = pm.getProperty(collectorPrefix + "SOCKSproxy.enabled", "0").equals("1")
                ? pm.getProperty(collectorPrefix + "SOCKSproxy.HostPort") : null;
        validateSOCKSproxyHostPort();
        mainTableDownloadTimeout = Integer.parseInt(pm.getProperty(collectorPrefix + "download.publicationList.timeout", "120"));
        contentDownloadTimeout = Integer.parseInt(pm.getProperty(collectorPrefix + "download.content.timeout", "120"));
        contentThreadPoolSize = Integer.parseInt(pm.getProperty(collectorPrefix + "download.content.threadPoolSize", "10"));

        setSourceDBId();
        setProxy();
    }

    private void validateSOCKSproxyHostPort() throws InitializationException {
        if (SOCKSproxyHostPort == null) {
            return;
        }
        String[] parts = SOCKSproxyHostPort.split(":");
        if (parts.length != 2) {
            throw new InitializationException(String.format("SOCKSproxyHostPort [%s] is invalid", SOCKSproxyHostPort));
        }
        try {
            Integer.parseInt(parts[1]);
        } catch (NumberFormatException ex) {
            throw new InitializationException(String.format("SOCKSproxyHostPort [%s] is invalid", SOCKSproxyHostPort));
        }
    }
    public HashMap<String, ArrayList<Publication>> getJournalPublications() {
        return resultMap;
    }

    public void downloadJournalPublications() {
        LOGGER.info("Fetching journals started...");
        initialize();
        try {
            downloadJournals();
            LOGGER.info("Fetching publications list ended...");
            LOGGER.info("Fetching content started...");


            ExecutorService executor = Executors.newFixedThreadPool(contentThreadPoolSize);
            for (String contentLink = getContentLink();;) {
                if (contentLink == null) {
                    break;
                }
                ContentDownloader contentDownloader = new ContentDownloader(contentLink, contentDownloadTimeout, contentsQueue);
                executor.execute(contentDownloader);
                contentLink = getContentLink();
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
            }

            for (String contentLink : contentLinksQueue) {
                addContentToQueue(downloadHTML(LINK_PREFIX + contentLink, contentDownloadTimeout));
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        unsetProxy();

        while (!contentsQueue.isEmpty()) {
            addToList(new ContentTableParser(contentsQueue.poll()));
        }
        LOGGER.info("Fetching publications ended...");
    }

    private void downloadJournals() throws IOException {
        LOGGER.info(String.format("Fetching publications list for \"%s\" and +\"%s\" started...", titleKeys, abstractKeys));
        MainTableParser mainTableParser = new MainTableParser(downloadHTML(getLinkJournal(), mainTableDownloadTimeout));
        for (String contentLink : mainTableParser.getContentLinks()) {
            contentLinksQueue.add(contentLink);
        }
        LOGGER.info(String.format("Fetching publications list for \"%s\" and +\"%s\" ended... Downloaded %d content links.", titleKeys, abstractKeys, mainTableParser.getContentLinks().size()));
    }

    private synchronized String getContentLink() {
        return contentLinksQueue.isEmpty() ? null : contentLinksQueue.poll();
    }

    private String getLinkJournal() {
         String abstractField;
         String titleField;
        if(abstractKeys == null || abstractKeys.isEmpty())
        {
            abstractField = "ANY";
        } else {
            abstractField = "Abstract";
        }
        
        if(titleKeys == null || titleKeys.isEmpty())
        {
            titleField = "ANY";
        } else {
            titleField = "Title";
        }
        return String.format(SEARCH_JOURNAL_TEMPLATE, titleKeys, titleField, operatorToString(outerOperator), abstractKeys, abstractField);
    }

    private String downloadHTML(String htmlLink, int timeout) throws IOException {
        return Jsoup.connect(htmlLink).timeout(timeout * 1000).get().html();
    }

    private void addToList(ContentTableParser record) {
        try {
            String authorName = "";
            if (!record.getAuthors().isEmpty()) {
                authorName = record.getAuthors().get(0).toString();
            }

            Publication publication = new Publication(++tmp,
                    sourceDbDAO.getSourceDBById(sourceDBId),
                    record.getDOI(),
                    authorName,
                    record.getTitle(),
                    record.getSummary(),
                    record.getDOI(),
                    record.getJournalRef(),
                    record.getSourceInfo().getTitle(),
                    record.getSourceInfo().getVolumeId(),
                    record.getSourceInfo().getIssueId(),
                    record.getSourceInfo().getPageRange(),
                    record.getSourceInfo().getPublicationDate(),
                    record.getPDFLink(),
                    null);

            publication.setSourceDbId(sourceDBId);
            List<Author> authorList = new ArrayList<Author>();

            for (com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.Author author : record.getAuthors()) {
                authorList.add(new Author(tmp, author.toString()));
            }
            publication.setAuthors(authorList);
            publicationList.add(publication);
            addJournal(publication);

        } catch (SourceDbDoesNotExistException ex) {
            LOGGER.error(ex);
        } catch (ParseException ex) {
            LOGGER.error(ex);
        }
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

    public List<Publication> getPublications() {
        return publicationList;
    }

    private synchronized void addContentToQueue(String content) {
        contentsQueue.add(content);
    }

    private void setProxy() {
        if (SOCKSproxyHostPort != null) {
            LOGGER.debug("Setting SOCKS proxy to " + SOCKSproxyHostPort);
            System.setProperty("socksProxyHost", SOCKSproxyHostPort.split(":")[0]);
            System.setProperty("socksProxyPort", SOCKSproxyHostPort.split(":")[1]);
        }
    }

    private void unsetProxy() {
        if (SOCKSproxyHostPort != null) {
            LOGGER.debug("Unsetting SOCKS proxy");
            System.clearProperty("socksProxyHost");
            System.clearProperty("socksProxyPort");
        }
    }

    private void setSourceDBId() {
        try {
            sourceDBId = sourceDbDAO.getSourceIdByShortName(PMSEConstants.BWN_SHORT_NAME);
        } catch (SourceDbDoesNotExistException ex) {
            LOGGER.fatal("Database BWN has no ID assigned. Cancelling job...");
        }
    }
}