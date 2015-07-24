/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.services.datacollectorservice.bwn;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;

/**
 *
 * @author Kamciak
 */
public class ContentDownloader implements Runnable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ContentDownloader.class);
    private static final String LINK_PREFIX = "http://vls2.icm.edu.pl/";
    private final String contentLink;
    private final int contentDownloadTimeout;
    private Queue<String> contentsQueue = new LinkedList<String>();

    public ContentDownloader(String contentLink, int contentDownloadTimeout, Queue<String> contentsQueue) {
        this.contentLink = contentLink;
        this.contentDownloadTimeout = contentDownloadTimeout;
        this.contentsQueue = contentsQueue;
    }

    @Override
    public void run() {
        try {
            LOGGER.info(String.format("Downloading content %s%s started..", LINK_PREFIX, contentLink));
            addContentToQueue(downloadHTML(LINK_PREFIX + contentLink, contentDownloadTimeout));
            LOGGER.debug(String.format("Downloading content %s%s ended..", LINK_PREFIX, contentLink));
        } catch (IOException ex) {
            LOGGER.error("Downloading content failed", ex);
        }
    }

    private String downloadHTML(String htmlLink, int timeout) throws IOException {
        return Jsoup.connect(htmlLink).timeout(timeout * 1000).get().html();
    }

    private synchronized void addContentToQueue(String content) {
        contentsQueue.add(content);
    }
}
