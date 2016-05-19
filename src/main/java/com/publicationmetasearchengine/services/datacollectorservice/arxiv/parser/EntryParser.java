package com.publicationmetasearchengine.services.datacollectorservice.arxiv.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class EntryParser {

    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final Document entryDoc;

    public EntryParser(String entryXml) {
        entryDoc = Jsoup.parse(entryXml);
    }

    public String getID() {
        final String rawEntryId = entryDoc.select("id").text();
        return rawEntryId.substring(rawEntryId.lastIndexOf('/') + 1).split("v")[0];
    }

    public String getTitle() {
        return entryDoc.select("title").text();
    }

    public String getSummary() {
        return entryDoc.select("summary").text();
    }

    public String getDOI() {
        return !entryDoc.select("arxiv|doi").text().isEmpty()? entryDoc.select("arxiv|doi").text():null;
    }
    
    public String getJournalRef() {
        return !entryDoc.select("arxiv|journal_ref").text().isEmpty() ? entryDoc.select("arxiv|journal_ref").text() : null;
        
    }

    public Date getPublicationDate() {
        try {
            return DATE_FORMATTER.parse(entryDoc.select("published").text());
        } catch (ParseException ex) {
            return null;
        }
    }

    public List<String> getAuthorStrings() {
        List<String> result = new ArrayList<String>();
        for(Element authorString : entryDoc.select("author"))
            result.add(invertAuthorNameSurname(authorString.text()));
        return result;
    }

    public String getPDFLink() {
        return String.format("http://arxiv.org/pdf/%s.pdf", getID());
    }
    
    public String getOriginalPDFLink() {
        return entryDoc.select("link[title=pdf").first().attr("abs:href");
    }
    
    public String getPDF()
    {
        return !(getOriginalPDFLink() == null || getOriginalPDFLink().isEmpty()) ? getOriginalPDFLink() : getPDFLink();
    }

    public RawEntry getRawEntry() {
        return new RawEntry(
                getID(),
                getTitle(),
                getSummary(),
                getDOI(),
                getJournalRef(),
                getPublicationDate(),
                getAuthorStrings(),
                getPDF()
            );
    }

    private static String invertAuthorNameSurname(String nameSurname) {
        String[] parts = nameSurname.split(",")[0].trim().split(" ");
        StringBuilder surnameName = new StringBuilder(parts[parts.length-1]);
        for (int i = 0; i < parts.length-1; ++i) {
            surnameName.append(" ").append(parts[i]);
        }
        return surnameName.toString();
    }
}
