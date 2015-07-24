/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.services.datacollectorservice.bwn;

import com.publicationmetasearchengine.dao.properties.PropertiesManager;
import com.publicationmetasearchengine.dao.sourcedbs.SourceDbDAO;
import com.publicationmetasearchengine.dao.sourcedbs.exceptions.SourceDbDoesNotExistException;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.services.datacollectorservice.bwn.parser.ContentTableParser;
import com.publicationmetasearchengine.services.datacollectorservice.bwn.parser.MainTableParser;
import com.publicationmetasearchengine.utils.PMSEConstants;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import com.publicationmetasearchengine.data.Author;
import com.thoughtworks.xstream.InitializationException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author Kamciak
 */
@Configurable(preConstruction = true)
public class BWNAuthorCollector {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(BWNAuthorCollector.class);
    private String authorName;
    private static final String SEARCH_TEMPLATE = "http://vls2.icm.edu.pl/cgi-bin/"
            + "search.pl?SearchTemplate=search_form.expert&search_field=%s&"
            + "fields=Author&Database=elsevier_1990&Database=springer_1990&"
            + "Category=all_categories&ArticleType=Article&Language=&"
            + "daterange=yearsince&drsince=2000&drpast=none&fromyear=none&"
            + "toyear=none&Max=750&Start=1&Order=SORT+DATE+DESC&GetSearchResults=Submit+Query";
    @Autowired
    private SourceDbDAO sourceDbDAO;
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

    public BWNAuthorCollector(String authorName) {
        this.authorName = authorName;
    }

    private String prepareAuthorNameForSearch(String authorName) {
        List<String> authorData = new ArrayList<String>(Arrays.asList(authorName.replace("-", "_").split(" ")));
        return authorData.get(0) + "_" + authorData.get(1);
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

        authorName = prepareAuthorNameForSearch(authorName);
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

    public void downloadAuthorPublications() {
        LOGGER.info("Fetching authors started...");
        initialize();
        try {
            downloadAuthors();
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

    private void downloadAuthors() throws IOException {
        LOGGER.info(String.format("Fetching publications list for %s started...", authorName));
        MainTableParser mainTableParser = new MainTableParser(downloadHTML(getLinkForAuthor(), mainTableDownloadTimeout));
        for (String contentLink : mainTableParser.getContentLinks()) {
            contentLinksQueue.add(contentLink);
        }
        LOGGER.info(String.format("Fetching publications list for %s ended... Downloaded %d content links.", authorName, mainTableParser.getContentLinks().size()));
    }

    private synchronized String getContentLink() {
        return contentLinksQueue.isEmpty() ? null : contentLinksQueue.poll();
    }

    private String getLinkForAuthor() {
        return String.format(SEARCH_TEMPLATE, authorName);
    }

    private String downloadHTML(String htmlLink, int timeout) throws IOException {
        return Jsoup.connect(htmlLink).timeout(timeout * 1000).get().html();
    }

    private void addToList(ContentTableParser record) {
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
                    record.getPDFLink(),
                    null);

            publication.setSourceDbId(sourceDBId);
            List<Author> authorList = new ArrayList<Author>();

            for (com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.Author author : record.getAuthors()) {
                authorList.add(new Author(tmp, author.toString()));
            }
            publication.setAuthors(authorList);
            publicationList.add(publication);

        } catch (SourceDbDoesNotExistException ex) {
            LOGGER.error(ex);
        } catch (ParseException ex) {
            LOGGER.error(ex);
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
