package com.publicationmetasearchengine.services.datacollectorservice.wok.parser;

import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.Author;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.CategoryInfo;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.SourceInfo;
import com.publicationmetasearchengine.utils.DateUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class RecordParserTest {

    private static RecordParser recordParser;

    public RecordParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        URL url = RecordParserTest.class.getResource("/OneRecord.xml");
        BufferedReader br = new BufferedReader(new FileReader(new File(url.getFile())));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();

        recordParser = new RecordParser(sb.toString());
    }

    /**
     * Test of getTitle method, of class RecordParser.
     */
    @Test
    public void testGetTitle() {
        System.out.println("getTitle");
        assertEquals("The effect of adding cadmium and "
                + "lead alone or in combination to the "
                + "diet of pigs on their growth, carcase "
                + "composition and reproduction", recordParser.getTitle());
    }

    /**
     * Test of getId method, of class RecordParser.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        assertEquals("WOS:000185655500013", recordParser.getId());
    }

    /**
     * Test of getSourceInfo method, of class RecordParser.
     */
    @Test
    public void testGetSourceInfo() throws Exception {
        System.out.println("getSourceInfo");
        SourceInfo result = recordParser.getSourceInfo();
        assertEquals("JOURNAL OF THE SCIENCE OF FOOD AND AGRICULTURE", result.getTitle());
        assertEquals("83", result.getVolumeId());
        assertEquals("13", result.getIssueId());
        assertEquals("1357-1365", result.getPageRange());
        assertEquals(DateUtils.parseDateOnly("2003-10-01"), result.getPublicationDate());
    }

    /**
     * Test of getAuthors method, of class RecordParser.
     */
    @Test
    public void testGetAuthors() {
        System.out.println("getAuthors");
        List<Author> result = recordParser.getAuthors();
        assertEquals(3, result.size());
        assertEquals("Phillips", result.get(0).getLastName());
        assertEquals("Z", result.get(1).getFirstName());
        assertEquals("Kovacs", result.get(2).getLastName());
        assertEquals("B", result.get(2).getFirstName());
    }

    /**
     * Test of getCategoryInfo method, of class RecordParser.
     */
    @Test
    public void testGetCategoryInfo() {
        System.out.println("getCategoryInfo");
        CategoryInfo result = recordParser.getCategoryInfo();
        assertEquals(1, result.getHeadings().size());
        assertEquals(2, result.getSubheadings().size());
        assertEquals("Science & Technology", result.getHeadings().get(0));
        assertEquals("Physical Sciences", result.getSubheadings().get(1));
    }

    /**
     * Test of getAbstract method, of class RecordParser.
     */
    @Test
    public void testGetAbstract() {
        System.out.println("getAbstract");
        assertEquals(true, recordParser.getAbstract().startsWith("Limits for cadmium"));
        assertEquals(true, recordParser.getAbstract().endsWith("of Chemical Industry."));
    }

    /**
     * Test of getDOI method, of class RecordParser.
     */
    @Test
    public void testGetDOI() {
        System.out.println("getDOI");
        assertEquals("10.1002/jsfa.1548", recordParser.getDOI());
    }

}
