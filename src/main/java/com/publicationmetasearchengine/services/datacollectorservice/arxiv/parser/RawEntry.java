package com.publicationmetasearchengine.services.datacollectorservice.arxiv.parser;

import java.util.Date;
import java.util.List;

public class RawEntry {

    private final String id;
    private final String title;
    private final String summary;
    private final String doi;
    private final String journalRef;
    private final Date publicationDate;
    private final List<String> authors;
    private final String pdfLink;

    public RawEntry(String id, String title, String summary, String doi, String journalRef, Date publicationDate, List<String> authors, String pdfLink) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.doi = doi;
        this.journalRef = journalRef;
        this.publicationDate = publicationDate;
        this.authors = authors;
        this.pdfLink = pdfLink;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getDoi() {
        return doi;
    }
    
    public String getJournalRef() {
        return journalRef;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public String getPdfLink() {
        return pdfLink;
    }


}
