package com.publicationmetasearchengine.services.datacollectorservice.wok.parser;

import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.RawRecord;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WOKParser {

    private static final Logger LOGGER = Logger.getLogger(WOKParser.class);
    private final Document wokDoc;

    public WOKParser(String xmlResponse) {
        wokDoc = Jsoup.parse(xmlResponse);
        LOGGER.debug("\n=====WOK DOC========\n");
        LOGGER.debug(wokDoc);
    }

    public List<RawRecord> getRecords() {
        List<RawRecord> records = new ArrayList<RawRecord>();
        for(Element rawRecord : wokDoc.select("REC"))
            try {
                records.add(new RecordParser(rawRecord.toString()).getRecord());
            } catch (ParseException ex) {
                LOGGER.debug(ex);
            }

        return records;
    }
}
