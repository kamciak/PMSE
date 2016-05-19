package com.publicationmetasearchengine.services.datacollectorservice.wok;

import com.publicationmetasearchengine.dao.properties.PropertiesManager;
import com.publicationmetasearchengine.dao.sourcedbs.SourceDbDAO;
import com.publicationmetasearchengine.dao.sourcedbs.exceptions.SourceDbDoesNotExistException;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.WOKLiteParser;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.WOKParser;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.RawRecord;
import com.publicationmetasearchengine.utils.PMSEConstants;
import com.thomsonreuters.wokmws.cxf.auth.WOKMWSAuthenticate;
import com.thomsonreuters.wokmws.cxf.auth.WOKMWSAuthenticateService;
import com.thomsonreuters.wokmws.v3.woksearchlite.EditionDesc;
import com.thomsonreuters.wokmws.v3.woksearchlite.LiteRecord;
import com.thomsonreuters.wokmws.v3.woksearchlite.QueryParameters;
import com.thomsonreuters.wokmws.v3.woksearchlite.RetrieveParameters;
import com.thomsonreuters.wokmws.v3.woksearchlite.SearchResults;
import com.thomsonreuters.wokmws.v3.woksearchlite.SortField;
import com.thomsonreuters.wokmws.v3.woksearchlite.TimeSpan;
import com.thomsonreuters.wokmws.v3.woksearchlite.WokSearchLite;
import com.thomsonreuters.wokmws.v3.woksearchlite.WokSearchLiteService;
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
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
public class WoKAuthorCollector {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(WoKAuthorCollector.class);
    @Autowired
    private SourceDbDAO sourceDbDAO;
    private String SOCKSproxyHostPort;
    private Queue<LiteRecord> recordsQueue = new LinkedList<LiteRecord>();
    private Integer sourceDBId = null;
    private String authorName;
    private static int tmp = 1;
    private List<Publication> publicationList = new ArrayList<Publication>();
    private String password;
    private String username;

    public WoKAuthorCollector(String authorName) {
        this.authorName = authorName;
    }

    public void downloadAuthorPublications() {
        LOGGER.info("Fetching author publications started...");
        initialize();

        WOKMWSAuthenticateService wokMWSAuthenticateService = new WOKMWSAuthenticateService();
        WOKMWSAuthenticate authenticator = wokMWSAuthenticateService.getWOKMWSAuthenticatePort();
        BindingProvider bp = (BindingProvider) authenticator;
        Map<String, Object> bpContext = bp.getRequestContext();
        bpContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

        bpContext.put(javax.xml.ws.BindingProvider.USERNAME_PROPERTY, username);
        bpContext.put(javax.xml.ws.BindingProvider.PASSWORD_PROPERTY, password);

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
//            FullRecordSearchResults searchResults = searchService.search(getQueryParameters(authorName), retrieveParameters);
            SearchResults searchResults = searchService.search(getQueryParameters(authorName), retrieveParameters);
            LOGGER.info(String.format("Querying for %s... found %d records", authorName, searchResults.getRecordsFound()));
            LOGGER.debug(String.format("Retrieving for %s started...", authorName));
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
            LOGGER.debug(String.format("Retrieving for %s ended...", authorName));
            
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
//               
        LOGGER.info("Fetching publications ended...");
    }

    private void addToList(RawRecord record) {
        try {
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

        } catch (SourceDbDoesNotExistException ex) {
            LOGGER.error(ex);
        }
    }

    private QueryParameters getQueryParameters(String authorName) {
        QueryParameters qp = new QueryParameters();
        qp.setDatabaseId("WOS");
        qp.setUserQuery("AU=" + authorName); //Author name
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
    public List<Publication> getPublications() {
        return publicationList;
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
        setProxy();
        username = pm.getProperty(collectorPrefix  + "username");
        password = pm.getProperty(collectorPrefix + "password");
    }

    private void unsetProxy() {
        if (SOCKSproxyHostPort != null) {
            LOGGER.debug("Unsetting SOCKS proxy");
            System.clearProperty("socksProxyHost");
            System.clearProperty("socksProxyPort");
        }
    }
}
