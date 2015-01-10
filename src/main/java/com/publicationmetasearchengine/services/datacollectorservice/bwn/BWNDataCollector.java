package com.publicationmetasearchengine.services.datacollectorservice.bwn;

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
import com.publicationmetasearchengine.services.datacollectorservice.bwn.parser.ContentTableParser;
import com.publicationmetasearchengine.services.datacollectorservice.bwn.parser.MainTableParser;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.Author;
import com.publicationmetasearchengine.utils.DateUtils;
import com.publicationmetasearchengine.utils.PMSEConstants;
import com.thoughtworks.xstream.InitializationException;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.jsoup.Jsoup;
import static org.quartz.CronScheduleBuilder.*;
import org.quartz.Job;
import static org.quartz.JobBuilder.*;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.newTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

public class BWNDataCollector implements ServiceJobProvider, Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(BWNDataCollector.class);

    @Configurable(preConstruction = true)
    public static class BWNDataCollectorJob implements Job {

        public class ContentDownloader implements Runnable {

            private static final String LINK_PREFIX = "http://vls2.icm.edu.pl/";
            private final String contentLink;

            public ContentDownloader(String contentLink) {
                this.contentLink = contentLink;
            }

            @Override
            public void run() {
                try {
                    LOGGER.info(String.format("Downloading content %s%s started..", LINK_PREFIX, contentLink));
                    addContentToQueue(downloadHTML(LINK_PREFIX+contentLink, contentDownloadTimeout));
                    LOGGER.debug(String.format("Downloading content %s%s ended..", LINK_PREFIX, contentLink));
                } catch (IOException ex) {
                    LOGGER.error("Downloading content failed", ex);
                }
            }

        }

        private static final String SEARCH_TEMPLATE = "http://vls2.icm.edu.pl/cgi-bin/"
                + "search.pl?SearchTemplate=search_form.expert&search_field={DATE%%3D%s}&"
                + "fields=Any&Database=elsevier_1990&Database=springer_1990&"
                + "Category=all_categories&ArticleType=Article&Language=&"
                + "daterange=yearsince&drsince=1995&drpast=none&fromyear=none&"
                + "toyear=none&Max=750&Start=1&Order=SORT+DATE+DESC&GetSearchResults=Submit+Query";

        @Autowired
        private PublicationManager publicationManager;
        @Autowired
        private AuthorManager authorManager;
        @Autowired
        private SourceDbDAO sourceDbDAO;
        @Autowired
        private SourceTitleDAO sourceTitleDAO;

        private String SOCKSproxyHostPort;
        private int mainTableDownloadTimeout;
        private int contentDownloadTimeout;
        private int maxDaysBackwardsToDownload;
        private int contentThreadPoolSize;
        private Queue<String> contentLinksQueue = new LinkedList<String>();
        private Queue<String> contentsQueue = new LinkedList<String>();

        private Integer sourceDBId = null;

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                sourceDBId = sourceDbDAO.getSourceIdByShortName(PMSEConstants.BWN_SHORT_NAME);
            } catch (SourceDbDoesNotExistException ex) {
                LOGGER.fatal("Database BWN has no ID assigned. Cancelling job...");
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

            LOGGER.info("Fetching publications list started...");
            try {
                for (int dayBack = daysCountToFetch; dayBack > 0; --dayBack)
                    downloadDay(dayBack);
            } catch (IOException ex) {
                LOGGER.error(ex);
            }
            LOGGER.info("Fetching publications list ended...");
            LOGGER.info("Fetching content started...");
            ExecutorService executor = Executors.newFixedThreadPool(contentThreadPoolSize);
            for(String contentLink = getContentLink();;) {
                if (contentLink == null) break;
                ContentDownloader contentDownloader = new ContentDownloader(contentLink);
                executor.execute(contentDownloader);
                contentLink = getContentLink();
            }
            executor.shutdown();
            while(!executor.isTerminated()) {
            }
            if (SOCKSproxyHostPort != null) {
                LOGGER.debug("Unsetting SOCKS proxy");
                System.clearProperty("socksProxyHost");
                System.clearProperty("socksProxyPort");
            }
            LOGGER.info("Fetching content ended...");

            while(!contentsQueue.isEmpty())
                insertRecordIntoDB(new ContentTableParser(contentsQueue.poll()));

            LOGGER.info("Fetching publications ended...");
        }

        private synchronized String getContentLink() {
            return contentLinksQueue.isEmpty()?null:contentLinksQueue.poll();
        }

        private synchronized void addContentToQueue(String content) {
            contentsQueue.add(content);
        }

        private void insertRecordIntoDB(ContentTableParser record) {
            LOGGER.debug("Inserting " + record.getTitle());
            List<Integer> authorIds = new ArrayList<Integer>();
            for (Author author : record.getAuthors()) {
                String authorString = String.format("%s %s", author.getLastName(), author.getFirstName());
                try {
                    authorIds.add(authorManager.addNewAuthor(authorString));
                } catch (AuthorAlreadyExistException ex) {
                    try {
                        authorIds.add(authorManager.getAuthorIdByName(authorString));
                    } catch (AuthorDoesNotExistException ex1) {
                        if (authorString.length() > PMSEConstants.AUTHOR_MAX_NAME_LENGHT)
                            LOGGER.warn(String.format("Author name [%s] is to long", authorString));
                        else
                          LOGGER.fatal("Should not occure !!", ex1);
                        return;
                    }
                }
            }
            if (authorIds.isEmpty()) {
                LOGGER.warn("There is no author... Skipping this publication...");
                return ;
            }

            Integer sourceTitleId = null;
            try {
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
            } catch (ParseException ex) {
                LOGGER.warn(String.format("Unparsable date [%s]. Skipping this publication...", ex.getMessage()));
                return ;
            }

            Integer publicationId = null;
            try {
                publicationId = publicationManager.insertPublication(
                        sourceDBId,
                        record.getDOI(),
                        authorIds.get(0),
                        record.getTitle(),
                        record.getSummary(),
                        record.getDOI(),
                        sourceTitleId,
                        record.getSourceInfo().getVolumeId(),
                        record.getSourceInfo().getIssueId(),
                        record.getSourceInfo().getPageRange(),
                        record.getSourceInfo().getPublicationDate(),
                        record.getPDFLink()
                );
            } catch (PublicationAlreadyExistException ex) {
                LOGGER.warn(String.format("Publication [BWN - %s (%s)] already exists", record.getDOI(), record.getTitle()));
                return;
            } catch (ParseException ex) {
                LOGGER.fatal("Should not occure...", ex);
                return ;
            }
            authorManager.setPublicationAuthorsIds(publicationId, authorIds);
        }

        private int getDaysBackwardsToDownload() {
            Date lastestDateFromDb = publicationManager.getNewestPublicationDate(sourceDBId);
            if (lastestDateFromDb == null)
                return maxDaysBackwardsToDownload;
            int daysBetween = Days.daysBetween(
                    (new DateTime(lastestDateFromDb)).withTimeAtStartOfDay(),
                    (new DateTime()).withTimeAtStartOfDay()).getDays();
            LOGGER.debug("Number of days between today and last date in DB: " + daysBetween);
            return daysBetween-1;
        }

        private void downloadDay(int dayBackCount) throws IOException {
            Date dateToDownload = (new DateTime()).minusDays(dayBackCount).toDate();
            LOGGER.info(String.format("Fetching publications list for %s started...", DateUtils.formatDateOnly(dateToDownload)));
            MainTableParser mainTableParser = new MainTableParser(downloadHTML(getLinkForDate(dateToDownload), mainTableDownloadTimeout));
            for(String contentLink : mainTableParser.getContentLinks())
                contentLinksQueue.add(contentLink);
            LOGGER.info(String.format("Fetching publications list for %s ended... Downloaded %d content links.", DateUtils.formatDateOnly(dateToDownload), mainTableParser.getContentLinks().size()));
        }

        private String getLinkForDate(Date date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            return String.format(SEARCH_TEMPLATE, sdf.format(date));
        }

        private String downloadHTML(String htmlLink, int timeout) throws IOException {
            return Jsoup.connect(htmlLink).timeout(timeout*1000).get().html();
        }

        public void setSOCKSproxyHostPort(String SOCKSproxyHostPort) {
            this.SOCKSproxyHostPort = SOCKSproxyHostPort;
        }

        public void setMainTableDownloadTimeout(int mainTableDownloadTimeout) {
            this.mainTableDownloadTimeout = mainTableDownloadTimeout;
        }

        public void setContentDownloadTimeout(int contentDownloadTimeout) {
            this.contentDownloadTimeout = contentDownloadTimeout;
        }

        public void setMaxDaysBackwardsToDownload(int maxDaysBackwardsToDownload) {
            this.maxDaysBackwardsToDownload = maxDaysBackwardsToDownload;
        }

        public void setContentThreadPoolSize(int contentThreadPoolSize) {
            this.contentThreadPoolSize = contentThreadPoolSize;
        }
    }

    private String schedule;
    private String SOCKSproxyHostPort;
    private int mainTableDownloadTimeout;
    private int contentDownloadTimeout;
    private int maxDaysBackwardsToDownload;
    private int contentThreadPoolSize;

    @Override
    public void initialize(String settingsPrefix) throws InitializationException {
        final PropertiesManager pm = PropertiesManager.getInstance();
        schedule = pm.getProperty(settingsPrefix + "schedule");
        SOCKSproxyHostPort = pm.getProperty(settingsPrefix + "SOCKSproxy.enabled", "0").equals("1")?
            pm.getProperty(settingsPrefix + "SOCKSproxy.HostPort") : null;
        validateSOCKSproxyHostPort();
        mainTableDownloadTimeout = Integer.parseInt(pm.getProperty(settingsPrefix + "download.publicationList.timeout", "120"));
        contentDownloadTimeout = Integer.parseInt(pm.getProperty(settingsPrefix + "download.content.timeout", "120"));
        maxDaysBackwardsToDownload = Integer.parseInt(pm.getProperty(settingsPrefix + "maxDaysBackwards", "10"));
        contentThreadPoolSize = Integer.parseInt(pm.getProperty(settingsPrefix + "download.content.threadPoolSize", "10"));

        LOGGER.info("Schedule = " + schedule);
        LOGGER.info("Proxy = " + (SOCKSproxyHostPort != null ? SOCKSproxyHostPort : "disabled"));
        LOGGER.info("Max days to download = " + maxDaysBackwardsToDownload);
        LOGGER.info("Main table download timeout = " + mainTableDownloadTimeout);
        LOGGER.info("Content download timeout = " + contentDownloadTimeout);
        LOGGER.info("Content download thread pool size = " + contentThreadPoolSize);
        LOGGER.info("Initialized");
    }

    private void validateSOCKSproxyHostPort() throws InitializationException {
        if (SOCKSproxyHostPort == null)
            return ;
        String[] parts = SOCKSproxyHostPort.split(":");
        if (parts.length != 2)
            throw new InitializationException(String.format("SOCKSproxyHostPort [%s] is invalid", SOCKSproxyHostPort));
        try {
            Integer.parseInt(parts[1]);
        } catch (NumberFormatException ex) {
            throw new InitializationException(String.format("SOCKSproxyHostPort [%s] is invalid", SOCKSproxyHostPort));
        }
    }

    @Override
    public JobDetail getJobDetail() {
        return newJob(BWNDataCollectorJob.class)
                .withDescription("BWN DataCollector")
                .usingJobData("SOCKSproxyHostPort", SOCKSproxyHostPort)
                .usingJobData("mainTableDownloadTimeout", mainTableDownloadTimeout)
                .usingJobData("contentDownloadTimeout", contentDownloadTimeout)
                .usingJobData("maxDaysBackwardsToDownload", maxDaysBackwardsToDownload)
                .usingJobData("contentThreadPoolSize", contentThreadPoolSize)
                .build();
    }

    @Override
    public Trigger getTrigger() {
        return newTrigger()
                .withIdentity("BWN DataCollector", "DataCollectors")
                .withSchedule(cronSchedule(schedule))
                .build();
    }

}
