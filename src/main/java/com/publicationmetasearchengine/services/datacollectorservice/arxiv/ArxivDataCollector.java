package com.publicationmetasearchengine.services.datacollectorservice.arxiv;

import com.publicationmetasearchengine.dao.authors.exceptions.AuthorAlreadyExistException;
import com.publicationmetasearchengine.dao.authors.exceptions.AuthorDoesNotExistException;
import com.publicationmetasearchengine.dao.properties.PropertiesManager;
import com.publicationmetasearchengine.dao.publications.exceptions.PublicationAlreadyExistException;
import com.publicationmetasearchengine.dao.sourcedbs.SourceDbDAO;
import com.publicationmetasearchengine.dao.sourcedbs.exceptions.SourceDbDoesNotExistException;
import com.publicationmetasearchengine.management.authormanagement.AuthorManager;
import com.publicationmetasearchengine.management.publicationmanagement.PublicationManager;
import com.publicationmetasearchengine.services.ServiceJobProvider;
import com.publicationmetasearchengine.services.datacollectorservice.arxiv.parser.ArxivParser;
import com.publicationmetasearchengine.services.datacollectorservice.arxiv.parser.RawEntry;
import com.publicationmetasearchengine.services.datacollectorservice.arxiv.querybuilder.QueryBuilder;
import com.publicationmetasearchengine.utils.PMSEConstants;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
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

public class ArxivDataCollector implements ServiceJobProvider, Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ArxivDataCollector.class);


    @Configurable(preConstruction = true)
    public static class ArxivDataCollectorJob implements Job {

        private static List<String> CATEGORIES = Arrays.asList("stat.AP", "stat.CO", "stat.ML", "stat.ME", "stat.OT", "stat.TH", "q-bio.BM", "q-bio.CB",
                "q-bio.GN", "q-bio.MN", "q-bio.NC", "q-bio.OT", "q-bio.PE", "q-bio.QM", "q-bio.SC", "q-bio.TO",
                "q-fin.CP", "q-fin.GN", "q-fin.PM", "q-fin.PR", "q-fin.RM", "q-fin.ST", "q-fin.TR", "cs.AI",
                "cs.CL", "cs.CC", "cs.CE", "cs.CG", "cs.GT", "cs.CV", "cs.CY", "cs.CR", "cs.DS", "cs.DB", "cs.DL",
                "cs.DM", "cs.DC", "cs.ET", "cs.FL", "cs.GL", "cs.GR", "cs.AR", "cs.HC", "cs.IR", "cs.IT", "cs.LG",
                "cs.LO", "cs.MS", "cs.MA", "cs.MM", "cs.NI", "cs.NE", "cs.NA", "cs.OS", "cs.OH", "cs.PF", "cs.PL",
                "cs.RO", "cs.SI", "cs.SE", "cs.SD", "cs.SC", "cs.SY", "nlin.AO", "nlin.CG", "nlin.CD", "nlin.SI",
                "nlin.PS", "math.AG", "math.AT", "math.AP", "math.CT", "math.CA", "math.CO", "math.AC", "math.CV",
                "math.DG", "math.DS", "math.FA", "math.GM", "math.GN", "math.GT", "math.GR", "math.HO", "math.IT",
                "math.KT", "math.LO", "math.MP", "math.MG", "math.NT", "math.NA", "math.OA", "math.OC", "math.PR",
                "math.QA", "math.RT", "math.RA", "math.SP", "math.ST", "math.SG", "astro-ph.CO", "astro-ph.EP",
                "astro-ph.GA", "astro-ph.HE", "astro-ph.IM", "astro-ph.SR", "cond-mat.dis-nn", "cond-mat.mes-hall",
                "cond-mat.quant-gas", "cond-mat.mtrl-sci", "cond-mat.other", "cond-mat.soft", "cond-mat.stat-mech", "cond-mat.str-el",
                "cond-mat.supr-con", "physics.acc-ph", "physics.ao-ph", "physics.atom-ph", "physics.atm-clus", "physics.bio-ph",
                "physics.chem-ph", "physics.class-ph", "physics.comp-ph", "physics.data-an", "physics.flu-dyn", "physics.gen-ph",
                "physics.geo-ph", "physics.hist-ph", "physics.ins-det", "physics.med-ph", "physics.optics", "physics.ed-ph",
                "physics.soc-ph", "physics.plasm-ph", "physics.pop-ph", "physics.space-ph", "quant-ph", "gr-qc", "hep-ex",
                "hep-lat", "hep-ph", "hep-th", "math-ph", "nucl-EX", "nucl-TH");

        @Autowired
        private PublicationManager publicationManager;
        @Autowired
        private AuthorManager authorManager;
        @Autowired
        private SourceDbDAO sourceDbDAO;

        private int maxDaysBackwardsToDownload;
        private int fetchPackageSize;
        private int delayBetweenFetches;

        private Integer sourceDBId = null;

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                sourceDBId = sourceDbDAO.getSourceIdByShortName(PMSEConstants.ARXIV_SHORT_NAME);
            } catch (SourceDbDoesNotExistException ex) {
                LOGGER.fatal("Database Arxiv has no ID assigned. Cancelling job...");
                return;
            }

            LOGGER.info("Fetching all categories started...");
            Date afterDate = getDateAfterToDownload();
            for (String category : CATEGORIES) {
                fetchCategory(category, afterDate);
                try {
                    Thread.sleep(delayBetweenFetches);
                } catch (InterruptedException ex) {
                    LOGGER.fatal("Should not occure !!! Cancelling job...", ex);
                    return ;
                }
            }
            LOGGER.info("Fetching all categories ended...");
        }

        private void fetchCategory(String categoryString, Date afterDate) {
            LOGGER.info(String.format("Fetching of category %s started...", categoryString));

            LOGGER.debug("Date after = " + afterDate.toString());

            Stack<RawEntry> entryStack = new Stack<RawEntry>();

            try {
                for(int position = 0; ; position += fetchPackageSize) {
                    LOGGER.debug(String.format("Fetching %03d-%03d", position, position + fetchPackageSize -1));
                    ArxivParser arxivParser = new ArxivParser(downloadHTML((new QueryBuilder(categoryString, position, fetchPackageSize, false)).build()));
                    List<RawEntry> etnriesAfterDate = arxivParser.getEtnriesAfterDate(afterDate);
                    for (RawEntry rawEntry : etnriesAfterDate)
                        entryStack.add(rawEntry);

                    if (etnriesAfterDate.isEmpty())
                        break;
                }
            } catch (IOException ex) {
                LOGGER.error(ex);
            }

            while(!entryStack.isEmpty())
                insertRecordIntoDB(entryStack.pop());

            LOGGER.info(String.format("Fetching of category %s ended...", categoryString));
        }

        private void insertRecordIntoDB(RawEntry record) {
            LOGGER.debug("Inserting " + record.getTitle());
            List<Integer> authorIds = new ArrayList<Integer>();
            for (String authorString : record.getAuthors()) {
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

            Integer publicationId = null;
            try {
                publicationId = publicationManager.insertPublication(
                        sourceDBId,
                        record.getId(),
                        authorIds.get(0),
                        record.getTitle(),
                        record.getSummary(),
                        record.getDoi(),
                        record.getJournalRef(),
                        null,
                        null,
                        null,
                        null,
                        record.getPublicationDate(),
                        record.getPdfLink()
                );
            } catch (PublicationAlreadyExistException ex) {
                LOGGER.warn(String.format("Publication [Arxiv - %s (%s)] already exists", record.getId(), record.getTitle()));
                return ;
            }
            authorManager.setPublicationAuthorsIds(publicationId, authorIds);
        }

        private Date getDateAfterToDownload() {
            Date lastestDateFromDb = publicationManager.getNewestPublicationDate(sourceDBId);
            if (lastestDateFromDb!= null)
                return lastestDateFromDb;
            DateTime computedDate = (new DateTime()).minusDays(maxDaysBackwardsToDownload);
            return computedDate.withTimeAtStartOfDay().toDate();
        }

        private String downloadHTML(String htmlLink) throws IOException {
            return Jsoup.connect(htmlLink).timeout(120*1000).ignoreContentType(true).get().html();
        }

        public void setMaxDaysBackwardsToDownload(int maxDaysBackwardsToDownload) {
            this.maxDaysBackwardsToDownload = maxDaysBackwardsToDownload;
        }

        public void setFetchPackageSize(int fetchPackageSize) {
            this.fetchPackageSize = fetchPackageSize;
        }

        public void setDelayBetweenFetches(int delayBetweenFetches) {
            this.delayBetweenFetches = delayBetweenFetches;
        }
    }

    private String schedule;
    private int maxDaysBackwardsToDownload;
    private int fetchPackageSize;
    private int delayBetweenFetches;

    @Override
    public void initialize(String settingsPrefix) {
        final PropertiesManager pm = PropertiesManager.getInstance();
        schedule = pm.getProperty(settingsPrefix+"schedule");
        maxDaysBackwardsToDownload = Integer.parseInt(pm.getProperty(settingsPrefix + "maxDaysBackwards", "10"));
        fetchPackageSize = Integer.parseInt(pm.getProperty(settingsPrefix+"fetchPackageSize"));
        delayBetweenFetches = Integer.parseInt(pm.getProperty(settingsPrefix+"delayBetweenFetches"));

        LOGGER.info("Schedule = " + schedule);
        LOGGER.info("Max days to download = " + maxDaysBackwardsToDownload);
        LOGGER.info("Fetch package size = " + fetchPackageSize);
        LOGGER.info("Delay between fetches = " + delayBetweenFetches);
        LOGGER.info("Initialized");
    }

    @Override
    public JobDetail getJobDetail() {
        return newJob(ArxivDataCollectorJob.class)
                .withDescription("Arxiv DataCollector")
                .usingJobData("maxDaysBackwardsToDownload", maxDaysBackwardsToDownload)
                .usingJobData("fetchPackageSize", fetchPackageSize)
                .usingJobData("delayBetweenFetches", delayBetweenFetches)
                .build();
    }

    @Override
    public Trigger getTrigger() {
        return newTrigger()
                .withIdentity("Arxiv DataCollector", "DataCollectors")
                .withSchedule(cronSchedule(schedule))
                .build();
    }

}
