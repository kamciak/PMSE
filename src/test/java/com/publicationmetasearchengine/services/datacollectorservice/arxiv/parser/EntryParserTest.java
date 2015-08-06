package com.publicationmetasearchengine.services.datacollectorservice.arxiv.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class EntryParserTest {

    private static List<EntryParser> entryParsers = new ArrayList<EntryParser>();

    public EntryParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        URL url = EntryParserTest.class.getResource("/ArxivResponse.xml");
        BufferedReader br = new BufferedReader(new FileReader(new File(url.getFile())));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();

        entryParsers.clear();
        for(Element entry: Jsoup.parse(sb.toString()).select("entry"))
            entryParsers.add(new EntryParser(entry.toString()));
    }

    /**
     * Test of getID method, of class EntryParser.
     */
    @Test
    public void testGetID() {
        System.out.println("getID");
        String[] expResult = {
            "1403.5701",
            "1403.5618",
            "1403.5508",
            "1403.5169",
            "1403.5142",
            "1403.5029",
            "1403.4023",
            "1403.3807",
            "1403.3084",
            "1403.2541"
        };
        for (int i = 0; i < entryParsers.size(); ++i)
            assertEquals(expResult[i], entryParsers.get(i).getID());
    }

    /**
     * Test of getTitle method, of class EntryParser.
     */
    @Test
    public void testGetTitle() {
        System.out.println("getTitle");
        String[] expResult = {
            "Cortex simulation system proposal using distributed computer network  environments",
            "Belief-Rule-Based Expert Systems for Evaluation of E- Government: A Case  Study",
            "Towards Active Logic Programming",
            "Defuzzify firstly or finally: Dose it matter in fuzzy DEMATEL under  uncertain environment?",
            "Interactive Debugging of ASP Programs",
            "Network-based Transcript Quantification with RNA-Seq Data",
            "Simulation leagues: Analysis of competition formats",
            "Sensing Subjective Well-being from Social Media",
            "Emerging archetypes in massive artificial societies for literary  purposes using genetic algorithms",
            "Turing: Then, Now and Still Key"
        };
        for (int i = 0; i < entryParsers.size(); ++i)
            assertEquals(expResult[i], entryParsers.get(i).getTitle());
    }

    /**
     * Test of getSummary method, of class EntryParser.
     */
    @Test
    public void testGetSummary() {
        System.out.println("getSummary");
        String[][] expResult = {
            {"In the dawn of computer","dwells in our brains."},
            {"Little knowledge exists","uncertainty."},
            {"In this paper we present","and proactive behaviour."},
            {"Decision-Making Trial","step in fuzzy DEMATEL."},
            {"Broad application of answer","preferred explanation."},
            {"High-throughput mRNA sequencing","Genome Atlas (TCGA)."},
            {"The selection of an appropriate","and 2013 respectively."},
            {"Subjective Well","very low cost."},
            {"The creation of fictional","the given environment."},
            {"This paper looks at Turing's","system."}
        };
        for (int i = 0; i < entryParsers.size(); ++i) {
            assertEquals(""+i+" starts", true, entryParsers.get(i).getSummary().startsWith(expResult[i][0]));
            assertEquals(""+i+" ends", true, entryParsers.get(i).getSummary().endsWith(expResult[i][1]));
        }
    }

    /**
     * Test of getPublicationDate method, of class EntryParser.
     */
    @Test
    public void testGetPublicationDate() throws ParseException {
        System.out.println("getPublicationDate");
        Date[] expResult = {
            EntryParser.DATE_FORMATTER.parse("2014-03-22T20:30:55Z"),
            EntryParser.DATE_FORMATTER.parse("2014-03-22T05:56:26Z"),
            EntryParser.DATE_FORMATTER.parse("2014-03-21T16:22:17Z"),
            EntryParser.DATE_FORMATTER.parse("2014-03-20T15:28:29Z"),
            EntryParser.DATE_FORMATTER.parse("2014-03-20T14:22:58Z"),
            EntryParser.DATE_FORMATTER.parse("2014-03-20T02:35:15Z"),
            EntryParser.DATE_FORMATTER.parse("2014-03-17T08:22:12Z"),
            null,
            EntryParser.DATE_FORMATTER.parse("2014-03-12T18:35:43Z"),
            null
        };
        for (int i = 0; i < entryParsers.size(); ++i)
            assertEquals(expResult[i], entryParsers.get(i).getPublicationDate());
    }

    /**
     * Test of getDOI method, of class EntryParser.
     */
    @Test
    public void testGetDOI() {
        System.out.println("getDOI");
        String[] expResult = {
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "10.1007/978-3-642-29694-9_3"
        };
        for (int i = 0; i < entryParsers.size(); ++i)
            assertEquals(expResult[i], entryParsers.get(i).getDOI());
    }

    /**
     * Test of getAuthorStrings method, of class EntryParser.
     */
    @Test
    public void testGetAuthorStrings() {
        System.out.println("getAuthorStrings");
        int[] expResult = { 1,  4, 1, 4, 1, 7, 4, 5, 3, 1 };
        for (int i = 0; i < entryParsers.size(); ++i) {
            for(String s : entryParsers.get(i).getAuthorStrings())
            System.out.println(s);
            assertEquals(expResult[i], entryParsers.get(i).getAuthorStrings().size());
        }
    }

    /**
     * Test of getPDFLink method, of class EntryParser.
     */
    @Test
    public void testGetPDFLink() {
        System.out.println("getPDFLink");
        String[] expResult = {
            "http://arxiv.org/pdf/1403.5701.pdf",
            "http://arxiv.org/pdf/1403.5618.pdf",
            "http://arxiv.org/pdf/1403.5508.pdf",
            "http://arxiv.org/pdf/1403.5169.pdf",
            "http://arxiv.org/pdf/1403.5142.pdf",
            "http://arxiv.org/pdf/1403.5029.pdf",
            "http://arxiv.org/pdf/1403.4023.pdf",
            "http://arxiv.org/pdf/1403.3807.pdf",
            "http://arxiv.org/pdf/1403.3084.pdf",
            "http://arxiv.org/pdf/1403.2541.pdf"
        };
        for (int i = 0; i < entryParsers.size(); ++i) {
            assertEquals(expResult[i], entryParsers.get(i).getPDFLink());
        }
    }
}



