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

    public RawRecord(String id, String title, SourceInfo sourceInfo, List<Author> authors, CategoryInfo categoryInfo, String summary, String DOI) {
        this.id = id;
        this.title = title;
        this.sourceInfo = sourceInfo;
        this.authors = authors;
        this.categoryInfo = categoryInfo;
        this.summary = summary;
        this.DOI = DOI;
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

}
