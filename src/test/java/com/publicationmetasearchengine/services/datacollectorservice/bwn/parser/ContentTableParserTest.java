package com.publicationmetasearchengine.services.datacollectorservice.bwn.parser;

import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.Author;
import com.publicationmetasearchengine.utils.DateUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

public class ContentTableParserTest {

    private static List<ContentTableParser> contentTableParsers = new ArrayList<ContentTableParser>();

    public ContentTableParserTest() throws Exception {
        String[] inputFiles = new String[]
            {"/BWN-ContentTable.xml",
             "/BWN-ContentTable2.xml"};
        contentTableParsers.clear();
        for (String inputFile : inputFiles) {
            URL url = ContentTableParser.class.getResource(inputFile);
            BufferedReader br = new BufferedReader(new FileReader(new File(url.getFile())));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();

            contentTableParsers.add(new ContentTableParser(sb.toString()));
        }

    }

    @BeforeClass
    public static void setUpClass() {
    }

    /**
     * Test of getTitle method, of class ContentTableParser.
     */
    @Test
    public void testGetTitle() {
        System.out.println("getTitle");
        String[] expResult = {"Parallel algorithms for Burrows-Wheeler compression and decompression",
                              "Order-preserving matching"};
        for (int i = 0; i < contentTableParsers.size(); ++i)
            assertEquals(expResult[i], contentTableParsers.get(i).getTitle());
    }

    /**
     * Test of getAuthors method, of class ContentTableParser.
     */
    @Test
    public void testGetAuthors() {
        System.out.println("getAuthors");
        Integer[] expResultSize = {2, 8};
        String[][] expResult = {{"James A. Edwards","Uzi Vishkin"},
            {"Jinil Kim","Peter Eades"}};
        for (int i = 0; i < contentTableParsers.size(); ++i) {
            assertEquals(expResultSize[i].intValue(), contentTableParsers.get(i).getAuthors().size());
            Author author = contentTableParsers.get(i).getAuthors().get(0);
            assertEquals(expResult[i][0], String.format("%s %s", author.getFirstName(),author.getLastName()));
            author = contentTableParsers.get(i).getAuthors().get(1);
            assertEquals(expResult[i][1], String.format("%s %s", author.getFirstName(),author.getLastName()));
        }
    }

    /**
     * Test of getSummary method, of class ContentTableParser.
     */
    @Test
    public void testGetSummary() {
        System.out.println("getSummary");
        String[][] expResult = {{"We present work","commercial platforms."},
                                {"We introduce a new string","Shown time algorithm."}};
        for (int i = 0; i < contentTableParsers.size(); ++i) {
            assertEquals(true, contentTableParsers.get(i).getSummary().startsWith(expResult[i][0]));
            assertEquals(true, contentTableParsers.get(i).getSummary().endsWith(expResult[i][1]));
        }
    }

    /**
     * Test of getPDFLink method, of class ContentTableParser.
     */
    @Test
    public void testGetPDFLink() {
        System.out.println("getPDFLink");
        String expResult[] = {
            "http://vls2.icm.edu.pl/cgi-bin/sciserv.pl?collection=elsevier&journal=03043975&issue=v525inone_c&article=10_pafbcad&form=pdf&file=file.pdf",
            "http://vls2.icm.edu.pl/cgi-bin/sciserv.pl?collection=elsevier&journal=03043975&issue=v525inone_c&article=68_om&form=pdf&file=file.pdf"};
        for (int i = 0; i < contentTableParsers.size(); ++i) {
            assertEquals(expResult[i], contentTableParsers.get(i).getPDFLink());
        }
    }

    /**
     * Test of getSourceInfo method, of class ContentTableParser.
     */
    @Test
    public void testGetSourceInfo() throws Exception {
        System.out.println("getSourceInfo");
        String expResult[][] = {
            {"Theoretical Computer Science", "525", null, "10-22"},
            {"Theoretical Computer Science", "69", "52", "11064-11069"}
        };
        Date expResultDate[] = {
            DateUtils.parseDateOnly("2014-03-13"),
            DateUtils.parseDateOnly("2014-03-13")
        };

        for (int i = 0; i < contentTableParsers.size(); ++i) {
            assertEquals(expResult[i][0], contentTableParsers.get(i).getSourceInfo().getTitle());
            assertEquals(expResult[i][1], contentTableParsers.get(i).getSourceInfo().getVolumeId());
            assertEquals(expResult[i][2], contentTableParsers.get(i).getSourceInfo().getIssueId());
            assertEquals(expResult[i][3], contentTableParsers.get(i).getSourceInfo().getPageRange());
            assertEquals(expResultDate[i], contentTableParsers.get(i).getSourceInfo().getPublicationDate());
        }
    }

    /**
     * Test of getDOI method, of class ContentTableParser.
     */
    @Test
    public void testGetDOI() {
        System.out.println("getDOI");
        String expResult[] = {
            "10.1016/j.tcs.2013.10.009",
            "10.1016/j.tcs.2013.10.006"
        };
        for (int i = 0; i < contentTableParsers.size(); ++i) {
            assertEquals(expResult[i], contentTableParsers.get(i).getDOI());
        }
    }

    @Test
    public void testExctactDOI() {
        System.out.println("extractDOI");
        String inputs[] = {
            "10.1016/j.jhazmat.2013.12.037",
            "27047 10.1016/j.apsusc.2014.01.039 S0169-4332(14)00067-1",
            "15625 10.1016/j.jhazmat.2013.12.032 S0304-3894(13)00962-X",
            "27106 10.1016/j.apsusc.2014.01.098 S0169-4332(14)00138-X",
            "8094 10.1016/j.psychres.2014.01.013 S0165-1781(14)00049-3",
            "10.1016/j.pscychresns.2013.12.001 S0925-4927(13)00318-1",
            "6818 10.1016/j.jneumeth.2014.01.024 S0165-0270(14)00035-1",
            "10159 10.1016/j.pscychresns.2014.01.004 S0925-4927(14)00005-5",
            "10160 10.1016/j.pscychresns.2014.01.005",
            "10153 10.1016/j.pscychresns.2013.12.004 S0925-4927(13)00321-1",
            "8050 10.1016/j.psychres.2013.12.026 S0165-1781(13)00819-6",
            "15548 10.1016/j.jhazmat.2013.11.023 S0304-3894(13)00862-5",
            "",
            "15714 S0304-3894(14)00075-2",
            "15630 10.1016/j.jhazmat.2013.12.037 S0304-3894(13)00967-9"
        };
        String expResult[] = {
            "10.1016/j.jhazmat.2013.12.037",
            "10.1016/j.apsusc.2014.01.039",
            "10.1016/j.jhazmat.2013.12.032",
            "10.1016/j.apsusc.2014.01.098",
            "10.1016/j.psychres.2014.01.013",
            "10.1016/j.pscychresns.2013.12.001",
            "10.1016/j.jneumeth.2014.01.024",
            "10.1016/j.pscychresns.2014.01.004",
            "10.1016/j.pscychresns.2014.01.005",
            "10.1016/j.pscychresns.2013.12.004",
            "10.1016/j.psychres.2013.12.026",
            "10.1016/j.jhazmat.2013.11.023",
            "null",
            "null",
            "10.1016/j.jhazmat.2013.12.037"
        };
        for (int i = 0; i < contentTableParsers.size(); ++i) {
            assertEquals(expResult[i], ContentTableParser.extractDOI(inputs[i]));
        }
    }

}
