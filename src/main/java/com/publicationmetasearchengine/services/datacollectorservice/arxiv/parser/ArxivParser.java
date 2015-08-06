package com.publicationmetasearchengine.services.datacollectorservice.arxiv.parser;

import com.publicationmetasearchengine.services.ServiceManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ArxivParser {

    private final Document arxivDoc;
    private static final Logger LOGGER = Logger.getLogger(ArxivParser.class);
    public ArxivParser(String xmlString) {
        this.arxivDoc = Jsoup.parse(xmlString);
        LOGGER.debug("\n=====ARXIV DOC========\n");
        LOGGER.debug(arxivDoc);
    }

    public List<RawEntry> getEtnriesAfterDate(Date afterDate) {
        List<RawEntry> entries = new ArrayList<RawEntry>();
        for(Element rawEntry : arxivDoc.select("entry")) {
            EntryParser entryParser = new EntryParser(rawEntry.toString());
            if (entryParser.getPublicationDate() == null || entryParser.getPublicationDate().before(afterDate))
                continue;
            entries.add(entryParser.getRawEntry());
        }

        return entries;
    }
    
    public List<RawEntry> getAuthorEntries(){
        List<RawEntry> entries = new ArrayList<RawEntry>();
        for(Element rawEntry : arxivDoc.select("entry")) {
            EntryParser entryParser = new EntryParser(rawEntry.toString());
            entries.add(entryParser.getRawEntry());
        }
        return entries;
    }
}
