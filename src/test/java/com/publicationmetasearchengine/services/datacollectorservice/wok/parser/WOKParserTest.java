package com.publicationmetasearchengine.services.datacollectorservice.wok.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

public class WOKParserTest {


    private static WOKParser wokParser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        URL url = WOKParserTest.class.getResource("/FullSearchRS.xml");
        BufferedReader br = new BufferedReader(new FileReader(new File(url.getFile())));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            sb.append(line);
        br.close();

        wokParser = new WOKParser(sb.toString());
    }

    public WOKParserTest() {

    }

    /**
     * Test of getTitle method, of class WOKParser.
     */
    @Test
    public void testGetTitle() {
        System.out.println("getTitle");
        assertEquals(5, wokParser.getRecords().size());
    }

}
