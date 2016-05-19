/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.services.datacollectorservice.wok.parser;

import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.RawRecord;
import com.thomsonreuters.wokmws.v3.woksearchlite.LiteRecord;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Kamciak
 */
public class WOKLiteParser {

    private static final Logger LOGGER = Logger.getLogger(WOKLiteParser.class);
    private final List<LiteRecord> liteRecords;

    public WOKLiteParser(List<LiteRecord> records) {
        liteRecords = records;
    }

    public List<RawRecord> getRecords() {
        List<RawRecord> records = new ArrayList<RawRecord>();
        int i = 0;
        LOGGER.debug("Do sparsowania: " + liteRecords.size());
        for (LiteRecord liteRecord : liteRecords) {
            try {
                LOGGER.debug("Parsing: " + i);
                records.add(new RecordLiteParser(liteRecord).getRecord());
                LOGGER.debug("Done Parsing: " + i);
                i++;
            } catch (ParseException ex) {
                LOGGER.debug(ex);
            }
        }
        LOGGER.debug("--------------------- wychodze z getRecord dla WOKLITEPARSER");
        return records;
    }
}
