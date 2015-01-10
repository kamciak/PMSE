package com.publicationmetasearchengine.services.datacollectorservice.bwn.parser;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MainTableParser {

    private final Document tableDoc;

    public MainTableParser(String xmlString) {
        tableDoc = Jsoup.parse(xmlString);
    }

    public List<String> getContentLinks() {
        List<String> result = new ArrayList<String>();
        for(Element element : tableDoc.select("a:containsOwn(Bibliographic Page)"))
            result.add(element.attr("href"));
        return result;
    }
}
