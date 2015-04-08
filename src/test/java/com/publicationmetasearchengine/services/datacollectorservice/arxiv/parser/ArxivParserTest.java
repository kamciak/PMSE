package com.publicationmetasearchengine.services.datacollectorservice.arxiv.parser;

import com.publicationmetasearchengine.utils.DateUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.text.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArxivParserTest {

    private static ArxivParser arxivParser;

    public ArxivParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        URL url = ArxivParserTest.class.getResource("/ArxivResponse.xml");
        BufferedReader br = new BufferedReader(new FileReader(new File(url.getFile())));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();

        arxivParser = new ArxivParser(sb.toString());
    }

    /**
     * Test of getEtnriesAfterDate method, of class ArxivParser.
     */
    @Test
    public void testGetEtnriesAfterDate() throws ParseException {
        System.out.println("getEtnriesAfterDate");
        assertEquals(8, arxivParser.getEtnriesAfterDate(DateUtils.parseDateOnly("2014-03-12")).size());
        assertEquals(7, arxivParser.getEtnriesAfterDate(DateUtils.parseDate("2014-03-12 19:00:00")).size());
        assertEquals(3, arxivParser.getEtnriesAfterDate(DateUtils.parseDateOnly("2014-03-21")).size());
        assertEquals(0, arxivParser.getEtnriesAfterDate(DateUtils.parseDate("2014-03-22 21:00:00")).size());
    }

}
