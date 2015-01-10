package com.publicationmetasearchengine.services.datacollectorservice.bwn.parser;

import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.Author;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.SourceInfo;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ContentTableParser {

    private final Document tableDoc;

    public ContentTableParser(String xmlString) {
        tableDoc = Jsoup.parse(xmlString);
    }

    public String getTitle() {
        return tableDoc.select("meta[NAME=title]").first().attr("content");
    }

    public List<Author> getAuthors() {
        final String authorsString = tableDoc.select("meta[NAME=Author]").first().attr("content");
        List<Author> authors = new ArrayList<Author>();
        for(String rawAuthor : authorsString.split(";")) {
            String[] authorParts = rawAuthor.split("\\,");
            if (authorParts.length < 2)
                continue;
            authors.add(new Author(authorParts[1].trim(), authorParts[0].trim()));
        }

        return authors;
    }

    public String getSummary() {
        Elements elements = tableDoc.select("b:containsOwn(Abstract (English):)");
        if (elements == null || elements.isEmpty())
            return "No abstract";
        return elements.parents().get(2).child(1).text();
    }

    public String getPDFLink() {
        String link = tableDoc.select("font:matchesOwn(Bibliographic Page)")
                .parents().get(0).child(2).child(2).attr("href");//workaround because of encodign problem when downloading page from service
        if (link==null || link.isEmpty())
            return null;
        return "http://vls2.icm.edu.pl"+link;
    }

    public String getDOI() {
        return extractDOI(tableDoc.select("b:matches(Item Identifier[s]{0,1}:)").parents().get(2).child(1).text());
    }

    public SourceInfo getSourceInfo() throws ParseException {
        Element sourceInfoParent = tableDoc.select("b > a").first().parents().get(4);
        String volumeIssueString = sourceInfoParent.select("tr").get(1).select("td").first().select("font > a").text();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        return new SourceInfo(
                tableDoc.select("b > a").first().text(),
                volumeIssueString.split(",")[0].split(" ")[1],
                volumeIssueString.contains("Issue") ? volumeIssueString.split("Issue: ")[1] : null,
                sourceInfoParent.select("tr").get(2).text().split("\\. ")[1],
                dateFormatter.parse(tableDoc.select("meta[NAME=Date.ANSIX3.30]").first().attr("content")));
    }

    public static String extractDOI(String input) {
        String[] parts = input.split(" ");
        for (String part : parts) {
            if (part.startsWith("10."))
                return part;
        }
        return null;
    }
}
