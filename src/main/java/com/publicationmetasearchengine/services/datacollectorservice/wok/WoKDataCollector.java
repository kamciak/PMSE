package com.publicationmetasearchengine.services.datacollectorservice.wok;

import com.publicationmetasearchengine.dao.authors.exceptions.AuthorAlreadyExistException;
import com.publicationmetasearchengine.dao.authors.exceptions.AuthorDoesNotExistException;
import com.publicationmetasearchengine.dao.properties.PropertiesManager;
import com.publicationmetasearchengine.dao.publications.exceptions.PublicationAlreadyExistException;
import com.publicationmetasearchengine.dao.sourcedbs.SourceDbDAO;
import com.publicationmetasearchengine.dao.sourcedbs.exceptions.SourceDbDoesNotExistException;
import com.publicationmetasearchengine.dao.sourcetitles.SourceTitleDAO;
import com.publicationmetasearchengine.dao.sourcetitles.exceptions.SourceTitleAlreadyExists;
import com.publicationmetasearchengine.dao.sourcetitles.exceptions.SourceTitleDoesNotExists;
import com.publicationmetasearchengine.management.authormanagement.AuthorManager;
import com.publicationmetasearchengine.management.publicationmanagement.PublicationManager;
import com.publicationmetasearchengine.services.ServiceJobProvider;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.WOKLiteParser;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.Author;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.RawRecord;
import com.publicationmetasearchengine.utils.DateUtils;
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
import com.thoughtworks.xstream.InitializationException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.CronScheduleBuilder.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

public class WoKDataCollector implements ServiceJobProvider, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(WoKDataCollector.class);

    @Configurable(preConstruction = true)
    public static class WoKDataCollectorJob implements Job {

        @Autowired
        private PublicationManager publicationManager;
        @Autowired
        private AuthorManager authorManager;
        @Autowired
        private SourceDbDAO sourceDbDAO;
        @Autowired
        private SourceTitleDAO sourceTitleDAO;
        private String SOCKSproxyHostPort;
        private int maxDaysBackwardsToDownload;
        private Queue<LiteRecord> recordsQueue = new LinkedList<LiteRecord>();
        private Integer sourceDBId = null;
        private String username;
        private String password;

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                sourceDBId = sourceDbDAO.getSourceIdByShortName(PMSEConstants.WOK_SHORT_NAME);
            } catch (SourceDbDoesNotExistException ex) {
                LOGGER.fatal("Database WOK has no ID assigned. Cancelling job...");
                return;
            }
            int daysCountToFetch = getDaysBackwardsToDownload();
            if (daysCountToFetch == 0) {
                LOGGER.info("Database is up to date. Exitting...");
                return;
            }
            LOGGER.debug(daysCountToFetch + " days to fetch...");

            LOGGER.info("Fetching publications started...");

            if (SOCKSproxyHostPort != null) {
                LOGGER.debug("Setting SOCKS proxy to " + SOCKSproxyHostPort);
                System.setProperty("socksProxyHost", SOCKSproxyHostPort.split(":")[0]);
                System.setProperty("socksProxyPort", SOCKSproxyHostPort.split(":")[1]);
            }

            WOKMWSAuthenticateService wokMWSAuthenticateService = new WOKMWSAuthenticateService();
            WOKMWSAuthenticate authenticator = wokMWSAuthenticateService.getWOKMWSAuthenticatePort();
            BindingProvider bp = (BindingProvider) authenticator;
            Map<String, Object> bpContext = bp.getRequestContext();
            bpContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

            bpContext.put(javax.xml.ws.BindingProvider.USERNAME_PROPERTY, username);
            bpContext.put(javax.xml.ws.BindingProvider.PASSWORD_PROPERTY, password);
            
            LOGGER.debug("Login do WOK: " + username);
            LOGGER.debug("Haslo do WOK: " + password);

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

                for (int dayBack = daysCountToFetch; dayBack > 0; --dayBack) {
                    downloadDay(searchService, dayBack);
                }

            } catch (Exception ex) {
                LOGGER.fatal(ex);
            } finally {
                try {
                    authenticator.closeSession();
                } catch (Exception ex) {
                }
                LOGGER.debug("Session closed");
            }

            if (SOCKSproxyHostPort != null) {
                LOGGER.debug("Unsetting SOCKS proxy");
                System.clearProperty("socksProxyHost");
                System.clearProperty("socksProxyPort");
            }

//            while (!recordsQueue.isEmpty()) {
                for (RawRecord rawRecord : new WOKLiteParser((List)recordsQueue).getRecords()) {
                    LOGGER.debug("--------->Tu wsadzam do bazy");
                    insertRecordIntoDB(rawRecord);
                }
//            }
            LOGGER.info("Fetching publications ended...");
        }

        private int getDaysBackwardsToDownload() {
            Date lastestDateFromDb = publicationManager.getNewestPublicationDate(sourceDBId);
            if (lastestDateFromDb == null) {
                return maxDaysBackwardsToDownload;
            }
            int daysBetween = Days.daysBetween(
                    (new DateTime(lastestDateFromDb)).withTimeAtStartOfDay(),
                    (new DateTime()).withTimeAtStartOfDay()).getDays();

            if (daysBetween > maxDaysBackwardsToDownload) {
                daysBetween = maxDaysBackwardsToDownload;
            }
            LOGGER.debug("Number of days between today and last date in DB: " + daysBetween);
            return daysBetween - 1;
        }

        private void insertRecordIntoDB(RawRecord record) {
            LOGGER.info("Inserting " + record.getTitle());
            List<Integer> authorIds = new ArrayList<Integer>();
            for (Author author : record.getAuthors()) {
                String authorString = String.format("%s %s", author.getLastName(), author.getFirstName());
                try {
                    authorIds.add(authorManager.addNewAuthor(authorString));
                } catch (AuthorAlreadyExistException ex) {
                    try {
                        authorIds.add(authorManager.getAuthorIdByName(authorString));
                    } catch (AuthorDoesNotExistException ex1) {
                        if (authorString.length() > PMSEConstants.AUTHOR_MAX_NAME_LENGHT) {
                            LOGGER.warn(String.format("Author name [%s] is to long", authorString));
                        } else {
                            LOGGER.fatal("Should not occure !!", ex1);
                        }
                        return;
                    }
                }
            }

            Integer sourceTitleId = null;
            try {
                sourceTitleId = sourceTitleDAO.addTitle(record.getSourceInfo().getTitle());
            } catch (SourceTitleAlreadyExists ex) {
                try {
                    sourceTitleId = sourceTitleDAO.getTitleIdByTitle(record.getSourceInfo().getTitle());
                } catch (SourceTitleDoesNotExists ex1) {
                    LOGGER.fatal("Should not occure", ex1);
                    return;
                }
            }

            Integer publicationId = null;
            try {
                publicationId = publicationManager.insertPublication(
                        sourceDBId,
                        record.getId(),
                        authorIds.get(0),
                        record.getTitle(),
                        record.getSummary(),
                        record.getDOI(),
                        record.getJournalRef(),
                        sourceTitleId,
                        record.getSourceInfo().getVolumeId(),
                        record.getSourceInfo().getIssueId(),
                        record.getSourceInfo().getPageRange(),
                        record.getSourceInfo().getPublicationDate(),
                        null);
            } catch (PublicationAlreadyExistException ex) {
                LOGGER.warn(String.format("Publication [WOK - %s (%s)] already exists", record.getId(), record.getTitle()));
                return;
            }
            authorManager.setPublicationAuthorsIds(publicationId, authorIds);
        }

        private void downloadDay(WokSearchLite searchService, int dayBackCount) throws Exception {
            DateTime queryDate = new DateTime();
            queryDate = queryDate.minusDays(dayBackCount);
            String queryDateString = DateUtils.formatDateOnly(queryDate.toDate());

            RetrieveParameters retrieveParameters = getRetrieveParameters();
            SearchResults searchResults = searchService.search(getQueryParameters(queryDate, queryDateString), retrieveParameters);
            LOGGER.info(String.format("Querying for %s... found %d records", queryDateString, searchResults.getRecordsFound()));
            LOGGER.debug(String.format("Retrieving for %s started...", queryDateString));

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
            LOGGER.debug(String.format("Retrieving for %s ended...", queryDateString));
        }

        private QueryParameters getQueryParameters(DateTime dateQuery, String queryDateString) {
            QueryParameters qp = new QueryParameters();
            qp.setDatabaseId("WOS");
            qp.setUserQuery("PY=" + dateQuery.getYear()); //Year Published
            EditionDesc editionDesc = new EditionDesc();
            editionDesc.setCollection("WOS");
            editionDesc.setEdition("SCI");
            qp.getEditions().add(editionDesc);
            editionDesc = new EditionDesc();
            editionDesc.setCollection("WOS");
            editionDesc.setEdition("SSCI");
            qp.getEditions().add(editionDesc);

            TimeSpan ts = new TimeSpan();
            ts.setBegin(queryDateString);
            ts.setEnd(queryDateString);
            qp.setTimeSpan(ts);
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

        public void setMaxDaysBackwardsToDownload(int maxDaysBackwardsToDownload) {
            this.maxDaysBackwardsToDownload = maxDaysBackwardsToDownload;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
    private String schedule;
    private String SOCKSproxyHostPort;
    private int maxDaysBackwardsToDownload;
    private String username;
    private String password;

    @Override
    public void initialize(String settingsPrefix) throws InitializationException {
        final PropertiesManager pm = PropertiesManager.getInstance();
        schedule = pm.getProperty(settingsPrefix + "schedule");
        SOCKSproxyHostPort = pm.getProperty(settingsPrefix + "SOCKSproxy.enabled", "0").equals("1")
                ? pm.getProperty(settingsPrefix + "SOCKSproxy.HostPort") : null;
        validateSOCKSproxyHostPort();
        maxDaysBackwardsToDownload = Integer.parseInt(pm.getProperty(settingsPrefix + "maxDaysBackwards", "30"));

        username = pm.getProperty(settingsPrefix + "username");
        password = pm.getProperty(settingsPrefix + "password");

        LOGGER.info("Schedule = " + schedule);
        LOGGER.info("Proxy = " + (SOCKSproxyHostPort != null ? SOCKSproxyHostPort : "disabled"));
        LOGGER.info("Max days to download = " + maxDaysBackwardsToDownload);
        LOGGER.info("Username = " + username);
        LOGGER.info("Initialized");
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

    @Override
    public JobDetail getJobDetail() {
        return newJob(WoKDataCollectorJob.class)
                .withDescription("WoK DataCollector")
                .usingJobData("SOCKSproxyHostPort", SOCKSproxyHostPort)
                .usingJobData("maxDaysBackwardsToDownload", maxDaysBackwardsToDownload)
                .usingJobData("username", username)
                .usingJobData("password", password)
                .build();
    }

    @Override
    public Trigger getTrigger() {
        return newTrigger()
                .withIdentity("WoK DataCollector", "DataCollectors")
                .withSchedule(cronSchedule(schedule))
                .build();
    }
}
