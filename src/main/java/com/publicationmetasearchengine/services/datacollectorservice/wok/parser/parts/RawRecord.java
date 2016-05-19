package com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts;

import java.util.List;

public class RawRecord {

    private final String id;
    private final String title;
    private final SourceInfo sourceInfo;
    private final List<Author> authors;
    private final CategoryInfo categoryInfo;
    private final String summary;
    private final String DOI;
    private final String journalRef;

    public RawRecord(String id, String title, SourceInfo sourceInfo, List<Author> authors, CategoryInfo categoryInfo, String summary, String DOI, String journalRef) {
        this.id = id;
        this.title = title;
        this.sourceInfo = sourceInfo;
        this.authors = authors;
        this.categoryInfo = categoryInfo;
        this.summary = summary;
        this.DOI = DOI;
        this.journalRef = journalRef;
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ");
        sb.append(this.title + "\n");
        sb.append("Id: ");
        sb.append(this.id + "\n");
        sb.append("SourceInfo ");
        sb.append(this.sourceInfo + "\n");
        sb.append("Authors: ");
        sb.append(this.authors + "\n");
        sb.append("CategoryInfo: ");
        sb.append(this.categoryInfo + "\n");
        sb.append("Summary: ");
        sb.append(this.summary + "\n");
        sb.append("Summary: ");
        sb.append(this.summary + "\n");
        sb.append("DOI: ");
        sb.append(this.DOI + "\n");
        sb.append("JournalRef: ");
        sb.append(this.journalRef + "\n");
        
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public SourceInfo getSourceInfo() {
        return sourceInfo;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public CategoryInfo getCategoryInfo() {
        return categoryInfo;
    }

    public String getSummary() {
        return summary;
    }

    public String getDOI() {
        return DOI;
    }
    
    public String getJournalRef() {
        return journalRef;
    }

}
