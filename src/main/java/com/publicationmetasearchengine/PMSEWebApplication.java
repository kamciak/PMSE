/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine;

import com.publicationmetasearchengine.gui.homescreen.HomeScreenPanel;
import com.publicationmetasearchengine.gui.loginscreen.LoginScreenPanel;
import com.publicationmetasearchengine.gui.notificationcriteriasscreen.NotificationCriteriasScreenPanel;
import com.publicationmetasearchengine.gui.profilescreen.ProfileScreenPanel;
import com.publicationmetasearchengine.gui.searchscreen.SearchScreenPanel;
import com.publicationmetasearchengine.gui.toreadscreen.ToReadScreenPanel;
import org.vaadin.navigator7.WebApplication;

/**
 *
 * @author Kamciak
 */
public class PMSEWebApplication extends WebApplication{
    public PMSEWebApplication()
    {
        registerPages(new Class[] {HomeScreenPanel.class, 
                                   LoginScreenPanel.class,
                                   ToReadScreenPanel.class,
                                   SearchScreenPanel.class,
                                   NotificationCriteriasScreenPanel.class,
                                   ProfileScreenPanel.class});
    }
}
