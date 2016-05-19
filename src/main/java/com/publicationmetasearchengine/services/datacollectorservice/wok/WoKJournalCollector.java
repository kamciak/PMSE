/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.services.datacollectorservice.wok;

import com.publicationmetasearchengine.dao.impactfactor.ImpactFactorDAO;
import com.publicationmetasearchengine.dao.properties.PropertiesManager;
import com.publicationmetasearchengine.dao.sourcedbs.SourceDbDAO;
import com.publicationmetasearchengine.dao.sourcedbs.exceptions.SourceDbDoesNotExistException;
import com.publicationmetasearchengine.data.Journal;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.WOKLiteParser;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.RawRecord;
import com.publicationmetasearchengine.utils.JournalComparator;
import com.publicationmetasearchengine.utils.PMSEConstants;
import com.thomsonreuters.wokmws.cxf.auth.WOKMWSAuthenticate;
import com.thomsonreuters.wokmws.cxf.auth.WOKMWSAuthenticateService;
import com.thomsonreuters.wokmws.v3.woksearchlite.EditionDesc;
import com.thomsonreuters.wokmws.v3.woksearchlite.LiteRecord;
import com.thomsonreuters.wokmws.v3.woksearchlite.QueryParameters;
import com.thomsonreuters.wokmws.v3.woksearchlite.RetrieveParameters;
import com.thomsonreuters.wokmws.v3.woksearchlite.SortField;
import com.thomsonreuters.wokmws.v3.woksearchlite.SearchResults;
import com.thomsonreuters.wokmws.v3.woksearchlite.WokSearchLite;
import com.thomsonreuters.wokmws.v3.woksearchlite.WokSearchLiteService;
import com.thoughtworks.xstream.InitializationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Pattern;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author Kamciak
 */

@Configurable(preConstruction = true)
public class WoKJournalCollector {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(WoKAuthorCollector.class);
    @Autowired
    private SourceDbDAO sourceDbDAO;
    @Autowired
    ImpactFactorDAO impactFactorDao;
    private String SOCKSproxyHostPort;
    private Queue<LiteRecord> recordsQueue = new LinkedList<LiteRecord>();
    private Integer sourceDBId = null;
    private static int tmp = 1;
    private List<Publication> publicationList = new ArrayList<Publication>();

    private String title = "";
    private HashMap<String, ArrayList<Publication>> resultMap = new HashMap<String, ArrayList<Publication>>();
    private HashMap<String, List<Journal>> journalsHashMapStartsAt = new HashMap<String, List<Journal>>();
    private String username = "";
    private String password = "";

    public WoKJournalCollector(List<String> titleKeys){
        for (String key : titleKeys) {
            title += key + " ";
        }
    }
    
    public List<Publication> getPublications() {
        return publicationList;
    }

    public HashMap<String, ArrayList<Publication>> getJournalPublications() {
        return resultMap;
    }

    public void downloadJournalPublications() {
        LOGGER.info("Fetching author publications started...");
        initialize();

        
            WOKMWSAuthenticateService wokMWSAuthenticateService = new WOKMWSAuthenticateService();
            WOKMWSAuthenticate authenticator = wokMWSAuthenticateService.getWOKMWSAuthenticatePort();
            BindingProvider bp = (BindingProvider) authenticator;
            Map<String, Object> bpContext = bp.getRequestContext();
            bpContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

            bpContext.put(javax.xml.ws.BindingProvider.USERNAME_PROPERTY, username);
            bpContext.put(javax.xml.ws.BindingProvider.PASSWORD_PROPERTY, password);
            
//        WOKMWSAuthenticateService wokMWSAuthenticateService = new WOKMWSAuthenticateService();
//        WOKMWSAuthenticate authenticator = wokMWSAuthenticateService.getWOKMWSAuthenticatePort();
//        BindingProvider bp = (BindingProvider) authenticator;
//        bp.getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

        try {
            
            LOGGER.debug("Authenticating... ");
            String cookie = String.format("SID=\"%s\"", authenticator.authenticate());
            LOGGER.debug("Obtained cookie: " + cookie);

            WokSearchLiteService wokSearchService = new WokSearchLiteService();
            WokSearchLite searchService = wokSearchService.getWokSearchLitePort();
            Map map = new HashMap();
            map.put("Cookie", Collections.singletonList(cookie));
            ((BindingProvider) searchService).getRequestContext().put(
                    MessageContext.HTTP_REQUEST_HEADERS, map);

            RetrieveParameters retrieveParameters = getRetrieveParameters();
            SearchResults searchResults = searchService.search(getQueryParameters(title), retrieveParameters);
//            FullRecordSearchResults searchResults = searchService.search(getQueryParameters(title), retrieveParameters);
            LOGGER.info(String.format("Querying for %s... found %d records", title, searchResults.getRecordsFound()));
            LOGGER.debug(String.format("Retrieving for \"%s\" started...", title));
             for (LiteRecord lr : searchResults.getRecords()) {
                recordsQueue.add(lr);
            }

            for (int i = 101; i < searchResults.getRecordsFound(); i = i + 100) {
                LOGGER.debug(String.format("Retriving %03d-%03d", i, i + 99));
                retrieveParameters.setFirstRecord(i);
                SearchResults retrieve = searchService.retrieve(searchResults.getQueryId(), retrieveParameters);
                for (LiteRecord lr : retrieve.getRecords()) {
                    recordsQueue.add(lr);
                }
            }            
            LOGGER.debug(String.format("Retrieving for \"%s\" ended...", title));
            
        } catch (Exception ex) {
            LOGGER.fatal(ex);
        } finally {
            try {
                authenticator.closeSession();
            } catch (Exception ex) {
            }
            LOGGER.debug("Session closed");
        }
        unsetProxy();

//            while (!recordsQueue.isEmpty()) {
                for (RawRecord rawRecord : new WOKLiteParser((List)recordsQueue).getRecords()) {
                    LOGGER.debug("--------->Tu wsadzam do bazy");
                    addToList(rawRecord);
                }
//            }        
        LOGGER.info("Fetching publications ended...");
    }

    private void addToList(RawRecord record) {
        try {
            String authorName = "";
            LOGGER.debug("Inserting " + record.getTitle());
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
                    null,
                    null);
            publication.setSourceDbId(sourceDBId);
            List<com.publicationmetasearchengine.data.Author> authorList = new ArrayList<com.publicationmetasearchengine.data.Author>();

            for (com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.Author author : record.getAuthors()) {
                authorList.add(new com.publicationmetasearchengine.data.Author(tmp, author.toString()));
            }
            publication.setAuthors(authorList);
            publicationList.add(publication);
            addJournal(publication);

        } catch (SourceDbDoesNotExistException ex) {
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

    private QueryParameters getQueryParameters(String title) {
        QueryParameters qp = new QueryParameters();
        qp.setDatabaseId("WOS");
        qp.setUserQuery("TI=" + title); //Title 
        EditionDesc editionDesc = new EditionDesc();
        editionDesc.setCollection("WOS");
        editionDesc.setEdition("SCI");
        qp.getEditions().add(editionDesc);
        editionDesc = new EditionDesc();
        editionDesc.setCollection("WOS");
        editionDesc.setEdition("SSCI");
        qp.getEditions().add(editionDesc);
        qp.setQueryLanguage("en");

        return qp;
    }

    private RetrieveParameters getRetrieveParameters() {
        RetrieveParameters retrieveParameters = new RetrieveParameters();
        retrieveParameters.setFirstRecord(1);
        retrieveParameters.setCount(100);
        SortField sortField = new SortField();
        sortField.setName("LD");    //Load Date
        sortField.setSort("A");     //Ascending
        retrieveParameters.getSortField().add(sortField);

        return retrieveParameters;
    }

    public void setSOCKSproxyHostPort(String SOCKSproxyHostPort) {
        this.SOCKSproxyHostPort = SOCKSproxyHostPort;
    }
    
    public void setUsername(String username)
    {
        this.username = username;
    }
    
    public void setPassword(String password)
    {
        this.password = password;
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

    private void setProxy() {
        if (SOCKSproxyHostPort != null) {
            LOGGER.debug("Setting SOCKS proxy to " + SOCKSproxyHostPort);
            System.setProperty("socksProxyHost", SOCKSproxyHostPort.split(":")[0]);
            System.setProperty("socksProxyPort", SOCKSproxyHostPort.split(":")[1]);
        }
    }

    private void initialize() throws InitializationException {
        final PropertiesManager pm = PropertiesManager.getInstance();
        final String collectorPrefix = "datacollector.WOK.";
        SOCKSproxyHostPort = pm.getProperty(collectorPrefix + "SOCKSproxy.enabled", "0").equals("1")
                ? pm.getProperty(collectorPrefix + "SOCKSproxy.HostPort") : null;
        validateSOCKSproxyHostPort();
        try {
            sourceDBId = sourceDbDAO.getSourceIdByShortName(PMSEConstants.WOK_SHORT_NAME);
        } catch (SourceDbDoesNotExistException ex) {
            LOGGER.fatal("Database WOK has no ID assigned. Cancelling job...");
        }
        username = pm.getProperty(collectorPrefix  + "username");
        password = pm.getProperty(collectorPrefix + "password");
        setProxy();
    }

    private void unsetProxy() {
        if (SOCKSproxyHostPort != null) {
            LOGGER.debug("Unsetting SOCKS proxy");
            System.clearProperty("socksProxyHost");
            System.clearProperty("socksProxyPort");
        }
    }
}
