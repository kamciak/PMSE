/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.utils.impactfactor;

import com.publicationmetasearchengine.data.Journal;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Kamciak
 */
public class ImpactFactorParser {

    private final Document journalDoc;
    private static final Logger LOGGER = Logger.getLogger(ImpactFactorParser.class);

    public ImpactFactorParser(String xmlString) {
        this.journalDoc = Jsoup.parse(xmlString);
    }

    public List<Journal> parse() {
        List<Journal> journals = new ArrayList<Journal>();
        Element impactFactorTable = getImpactFactorTable();
        List<Element> impactFactorTableTr = getTrForTable(impactFactorTable);
        impactFactorTableTr.remove(0); //header of table
        for (Element tr : impactFactorTableTr) {
            Journal journal = getJournalFromTr(tr);
            if (journal != null) {
                journals.add(journal);
            }
        }

        return journals;
    }

    public Element getImpactFactorTable() {
        return journalDoc.select("table[dir=LTR]").first();
    }

    public List<Element> getTrForTable(Element table) {
        return table.select("tr");
    }

    private Journal getJournalFromTr(Element tr) {
        List<Element> impactFactorTd = tr.select("td");

        try {
            Integer id = Integer.parseInt(impactFactorTd.get(0).text()); //pozniej nadpisane przez DAO
            String title = impactFactorTd.get(1).text();
            String ISSN = impactFactorTd.get(2).text();
            Float if2013_2014 = tryToParseImpactFactorValue(impactFactorTd.get(3).text());
            Float if2012 = tryToParseImpactFactorValue(impactFactorTd.get(4).text());
            Float if2011 = tryToParseImpactFactorValue(impactFactorTd.get(5).text());
            Float if2010 = tryToParseImpactFactorValue(impactFactorTd.get(6).text());
            Float if2009 = tryToParseImpactFactorValue(impactFactorTd.get(7).text());
            Float if2008 = tryToParseImpactFactorValue(impactFactorTd.get(8).text());

            return new Journal(id, title, ISSN, if2013_2014, if2012, if2011, if2010, if2009, if2008);
        } catch (NumberFormatException ex) {
            return null;
        }

    }

    private Float tryToParseImpactFactorValue(String value) {
        try {
            Float floatValue = Float.parseFloat(value);
            return floatValue;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
