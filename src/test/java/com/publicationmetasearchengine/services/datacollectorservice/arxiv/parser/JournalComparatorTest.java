/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.services.datacollectorservice.arxiv.parser;

import com.publicationmetasearchengine.data.Journal;
import com.publicationmetasearchengine.utils.JournalComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Kamciak
 */
public class JournalComparatorTest {
 
    public JournalComparatorTest()
    {
        
    }
    
    @Test
    public void testComparator()
    {
        final List<Journal> listToSort = new ArrayList<Journal>();
        

        listToSort.add(new Journal(1, "Short", "1", Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN));
        listToSort.add(new Journal(2, "The longest string in array", "1", Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN));
        listToSort.add(new Journal(3, "Midle length", "1", Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN));
        listToSort.add(new Journal(4, "A", "1", Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN));
        
        Collections.sort(listToSort, new JournalComparator());
        
        
        List<String> expResult = new ArrayList<String>();
        expResult.add("The longest string in array");
        expResult.add("Midle length");
        expResult.add("Short");
        expResult.add("A");
        
         for (int i = 0; i < listToSort.size(); ++i)
            assertEquals(expResult.get(i), listToSort.get(i).getTitle());
        
    }
    
}
