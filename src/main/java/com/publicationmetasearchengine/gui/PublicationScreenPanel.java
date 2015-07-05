/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.gui;

import com.publicationmetasearchengine.data.Publication;
import com.vaadin.ui.Table;
import java.util.List;

/**
 *
 * @author Kamciak
 */
public interface PublicationScreenPanel extends ScreenPanel{
    List<Publication> getPanelPublications();
    Publication getCurrentPublication();
    Table getPublicationTable();
    void setBackup();
}
