/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.gui.toreadscreen;

import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.utils.Notificator;
import com.vaadin.Application;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Kamciak
 */
public class BibTeXDownloader {
    private static final Logger LOGGER = Logger.getLogger(BibTeXDownloader.class);
    private final Application application;
    private final User user;
    
    public BibTeXDownloader(Application application, User user){
        this.application = application;
        this.user = user;
    }

    public void downloadBibTex(final List<String> bibtex)
    {
        LOGGER.info("Downloading BibTeX for user" + user.toString());
        try{
        StreamSource ss = new StreamSource() {
            InputStream is = new ByteArrayInputStream(convertBibTeXListToString(bibtex).getBytes());
            @Override
            public InputStream getStream() {
                return is;
            }
        };
        StreamResource sr = new StreamResource(ss, user.getLogin() + ".bib", application);
        application.getMainWindow().open(sr, "_blank");
        }
        catch(Exception ex){
            LOGGER.error("Download Error. Could not download BibTex file\n Cause: "+ex.getMessage());
            Notificator.showNotification(application, "Download Error", "Could not download BibTeX file", Notificator.NotificationType.ERROR);
        }
    }
    
    private String convertBibTeXListToString(List<String> bibtex){
        StringBuilder sb = new StringBuilder();
        for(String line : bibtex) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
