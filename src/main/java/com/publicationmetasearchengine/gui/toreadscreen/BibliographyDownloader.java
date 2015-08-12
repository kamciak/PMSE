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
public class BibliographyDownloader {
    private static final Logger LOGGER = Logger.getLogger(BibliographyDownloader.class);
    private final Application application;
    private final User user;
    
    public BibliographyDownloader(Application application, User user){
        this.application = application;
        this.user = user;
    }

    public void downloadBibliography(final List<String> bibliographyList, String extension)
    {
        LOGGER.info("Downloading bibliography for user" + user.toString());
        try{
        StreamSource ss = new StreamSource() {
            InputStream is = new ByteArrayInputStream(convertBibliographyListToString(bibliographyList).getBytes());
            @Override
            public InputStream getStream() {
                return is;
            }
        };
        StreamResource sr = new StreamResource(ss, user.getLogin() + extension, application);
        application.getMainWindow().open(sr, "_blank");
        }
        catch(Exception ex){
            LOGGER.error("Download Error. Could not download BibTex file\n Cause: "+ex.getMessage());
            Notificator.showNotification(application, "Download Error", "Could not download BibTeX file", Notificator.NotificationType.ERROR);
        }
    }
    
    private String convertBibliographyListToString(List<String> biliographyList){
        StringBuilder sb = new StringBuilder();
        for(String line : biliographyList) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
