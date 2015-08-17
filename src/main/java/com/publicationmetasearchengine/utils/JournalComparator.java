/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.utils;

import com.publicationmetasearchengine.data.Journal;
import java.util.Comparator;

/**
 *
 * @author Kamciak
 */
public class JournalComparator implements Comparator<Journal>{
    @Override
    public int compare(Journal lhs, Journal phs)
    {
       if (lhs.getTitle().length() < phs.getTitle().length()){
           return 1;
       } else if (lhs.getTitle().length() > phs.getTitle().length())
           return -1;
       else return 0;
    }
}
