package com.publicationmetasearchengine.gui.toreadscreen;

import com.publicationmetasearchengine.dao.properties.PropertiesManager;
import com.publicationmetasearchengine.dao.sourcedbs.SourceDbDAO;
import com.publicationmetasearchengine.dao.sourcedbs.exceptions.SourceDbDoesNotExistException;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.utils.Notificator;
import com.publicationmetasearchengine.utils.PMSEConstants;
import com.publicationmetasearchengine.utils.ZipUtils;
import com.vaadin.Application;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.lingala.zip4j.exception.ZipException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
public class PublicationDownloader {
    private static final Logger LOGGER = Logger.getLogger(PublicationDownloader.class);

    private final Application application;
    private final User user;
    final List<Publication> dowloadedPublications = new ArrayList<Publication>();

    @Autowired
    private SourceDbDAO sourceDbDAO;

    public PublicationDownloader(Application application, User user) {
        this.application = application;
        this.user = user;
    }

    public boolean downloadPublications(List<Publication> publications) {
        LOGGER.info("Downloading publications for user" + user.toString());
        LOGGER.debug(String.format("Found %d publications for user %s", publications.size(), user.toString()));
        final List<Publication> arxivPublications = new ArrayList<Publication>();
        final List<Publication> bwnPublications = new ArrayList<Publication>();
        for (Publication publication : publications) {
            if (publication.getPdfLink() != null && !publication.getPdfLink().isEmpty()) {
                try {
                    if (publication.getSourceDB().getId() == sourceDbDAO.getSourceIdByShortName(PMSEConstants.ARXIV_SHORT_NAME))
                        arxivPublications.add(publication);
                    else if (publication.getSourceDB().getId() == sourceDbDAO.getSourceIdByShortName(PMSEConstants.BWN_SHORT_NAME))
                        bwnPublications.add(publication);
                } catch (SourceDbDoesNotExistException ex) {
                }
            }
        }
        LOGGER.debug(String.format("Found %d arxiv publications with PDFs for user %s", arxivPublications.size(), user.toString()));
        LOGGER.debug(String.format("Found %d BWN publications with PDFs for user %s", bwnPublications.size(), user.toString()));
        if (arxivPublications.isEmpty() && bwnPublications.isEmpty()) {
            Notificator.showNotification(application, "No publications...", "There is no publications, that have PDF to download", Notificator.NotificationType.HUMANIZED);
            LOGGER.info(String.format("Downloading publications for user %s ended", user.toString()));
            return false;
        }

        final HashMap<String, InputStream> files = new HashMap<String, InputStream>();
        if (! arxivPublications.isEmpty())
            downloadPublications(arxivPublications, false, files);
        if (! bwnPublications.isEmpty())
            downloadPublications(bwnPublications, true, files);

        try {
            File zipFile = ZipUtils.zipPDFStreams(files, user.getLogin());
            TemporaryFileDownloadResource resource = new TemporaryFileDownloadResource(application,
                    zipFile.getName(), "application/zip", zipFile);
            application.getMainWindow().open(resource, "_self");
            LOGGER.info(String.format("Downloading publications for user %s ended", user.toString()));
            return true;
        } catch (ZipException ex) {
            LOGGER.error(ex);
            Notificator.showNotification(application, "Download Error", "Could not create zip file", Notificator.NotificationType.ERROR);
        } catch (FileNotFoundException ex) {
            LOGGER.error(ex);
            Notificator.showNotification(application, "Download Error", "Could not download zip file", Notificator.NotificationType.ERROR);
        }
        return false;
    }

    private void downloadPublications(List<Publication> publications, boolean useBWNProxy, HashMap<String, InputStream> files) {
        PropertiesManager pm = PropertiesManager.getInstance();
        String SOCKSproxyHostPort = null;
        if (useBWNProxy)
            SOCKSproxyHostPort = pm.getProperty("datacollector.BWN.SOCKSproxy.enabled", "0").equals("1")?
            pm.getProperty("datacollector.BWN.SOCKSproxy.HostPort") : null;
        if (SOCKSproxyHostPort != null) {
            LOGGER.debug("Setting SOCKS proxy to " + SOCKSproxyHostPort);
            System.setProperty("socksProxyHost", SOCKSproxyHostPort.split(":")[0]);
            System.setProperty("socksProxyPort", SOCKSproxyHostPort.split(":")[1]);
        }
        for (Publication publication : publications) {
            try {
                files.put(useBWNProxy?publication.getDoi():publication.getArticleId(), PDFDownloader.downloadPDF(publication.getPdfLink()));
                dowloadedPublications.add(publication);
            } catch (MalformedURLException ex) {
                LOGGER.error(ex);
            } catch (IOException ex) {
                LOGGER.error(ex);
            }
        }
        LOGGER.info(String.format("Downloaded %d publications for user %s", dowloadedPublications.size(), user.toString()));

        if (SOCKSproxyHostPort != null) {
            LOGGER.debug("Unsetting SOCKS proxy");
            System.clearProperty("socksProxyHost");
            System.clearProperty("socksProxyPort");
        }
    }

    public List<Publication> getDowloadedPublications() {
        return dowloadedPublications;
    }
}
