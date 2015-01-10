package com.publicationmetasearchengine.services.datacollectorservice.bwn.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

public class MainTableParserTest {

    private static MainTableParser mainTableParser;

    public MainTableParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        URL url = MainTableParserTest.class.getResource("/BWN-MainTable.xml");
        BufferedReader br = new BufferedReader(new FileReader(new File(url.getFile())));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();

        mainTableParser = new MainTableParser(sb.toString());
    }

    /**
     * Test of getContentLinks method, of class MainTableParser.
     */
    @Test
    public void testGetContentLinks() {
        System.out.println("getContentLinks");
        List<String> expResult = Arrays.asList(
            "/cgi-bin/sciserv.pl?collection=elsevier&journal=03043975&issue=v525inone_c&article=42_gsc&search_term=arttype%3D%28Article%29%20%28%20%28%7BDATE%3D20140313%7D%29%20%29%20%7BSORT%20DATE%20DESC%7D%20%7BDATE%3E%3D19950000%7D%20%7BTAG%3Dspringer%20cell-backfile%20elsevier%7D",
            "/cgi-bin/sciserv.pl?collection=elsevier&journal=03043975&issue=v525inone_c&article=10_pafbcad&search_term=arttype%3D%28Article%29%20%28%20%28%7BDATE%3D20140313%7D%29%20%29%20%7BSORT%20DATE%20DESC%7D%20%7BDATE%3E%3D19950000%7D%20%7BTAG%3Dspringer%20cell-backfile%20elsevier%7D",
            "/cgi-bin/sciserv.pl?collection=elsevier&journal=03043975&issue=v525inone_c&article=23_mss&search_term=arttype%3D%28Article%29%20%28%20%28%7BDATE%3D20140313%7D%29%20%29%20%7BSORT%20DATE%20DESC%7D%20%7BDATE%3E%3D19950000%7D%20%7BTAG%3Dspringer%20cell-backfile%20elsevier%7D",
            "/cgi-bin/sciserv.pl?collection=elsevier&journal=03043975&issue=v525inone_c&article=30_lsfspbieo&search_term=arttype%3D%28Article%29%20%28%20%28%7BDATE%3D20140313%7D%29%20%29%20%7BSORT%20DATE%20DESC%7D%20%7BDATE%3E%3D19950000%7D%20%7BTAG%3Dspringer%20cell-backfile%20elsevier%7D",
            "/cgi-bin/sciserv.pl?collection=elsevier&journal=03043975&issue=v525inone_c&article=3_otanoriaw&search_term=arttype%3D%28Article%29%20%28%20%28%7BDATE%3D20140313%7D%29%20%29%20%7BSORT%20DATE%20DESC%7D%20%7BDATE%3E%3D19950000%7D%20%7BTAG%3Dspringer%20cell-backfile%20elsevier%7D",
            "/cgi-bin/sciserv.pl?collection=elsevier&journal=03043975&issue=v525inone_c&article=55_notgpofdtc&search_term=arttype%3D%28Article%29%20%28%20%28%7BDATE%3D20140313%7D%29%20%29%20%7BSORT%20DATE%20DESC%7D%20%7BDATE%3E%3D19950000%7D%20%7BTAG%3Dspringer%20cell-backfile%20elsevier%7D",
            "/cgi-bin/sciserv.pl?collection=elsevier&journal=03043975&issue=v525inone_c&article=60_dapp&search_term=arttype%3D%28Article%29%20%28%20%28%7BDATE%3D20140313%7D%29%20%29%20%7BSORT%20DATE%20DESC%7D%20%7BDATE%3E%3D19950000%7D%20%7BTAG%3Dspringer%20cell-backfile%20elsevier%7D",
            "/cgi-bin/sciserv.pl?collection=elsevier&journal=03043975&issue=v525inone_c&article=89_itm&search_term=arttype%3D%28Article%29%20%28%20%28%7BDATE%3D20140313%7D%29%20%29%20%7BSORT%20DATE%20DESC%7D%20%7BDATE%3E%3D19950000%7D%20%7BTAG%3Dspringer%20cell-backfile%20elsevier%7D",
            "/cgi-bin/sciserv.pl?collection=elsevier&journal=03043975&issue=v525inone_c&article=68_om&search_term=arttype%3D%28Article%29%20%28%20%28%7BDATE%3D20140313%7D%29%20%29%20%7BSORT%20DATE%20DESC%7D%20%7BDATE%3E%3D19950000%7D%20%7BTAG%3Dspringer%20cell-backfile%20elsevier%7D",
            "/cgi-bin/sciserv.pl?collection=elsevier&journal=03043975&issue=v525inone_c&article=80_eawka&search_term=arttype%3D%28Article%29%20%28%20%28%7BDATE%3D20140313%7D%29%20%29%20%7BSORT%20DATE%20DESC%7D%20%7BDATE%3E%3D19950000%7D%20%7BTAG%3Dspringer%20cell-backfile%20elsevier%7D"
        );

        List<String> result = mainTableParser.getContentLinks();
        assertEquals(expResult, result);
    }

}
