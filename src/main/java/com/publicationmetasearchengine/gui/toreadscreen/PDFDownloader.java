package com.publicationmetasearchengine.gui.toreadscreen;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.log4j.Logger;

public class PDFDownloader {
    private static final Logger LOGGER = Logger.getLogger(PDFDownloader.class);

    public static InputStream downloadPDF(String link) throws MalformedURLException, IOException {
        LOGGER.debug(String.format("Downloading PDF from %s", link));
        URL url = new URL(link);
        URLConnection con = url.openConnection(); 
        BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
        LOGGER.debug(String.format("PDF downloaded: %s", link));
        return bis;

    }
}
