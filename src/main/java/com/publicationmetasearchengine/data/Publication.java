package com.publicationmetasearchengine.data;

import com.publicationmetasearchengine.dao.publications.exceptions.PublicationWithNoAuthorException;
import java.util.Date;
import java.util.List;

public class Publication {

    private Integer id;
    private Integer sourceDbId;
    private SourceDB sourceDB;
    private final String articleId;
    private final String mainAuthor;
    private final String title;
    private final String summary;
    private final String doi;
    private final String journalRef;
    private final String sourceTitle;
    private final String sourceVolume;
    private final String sourceIssue;
    private final String sourcePageRange;
    private final Date publicationDate;
    private final String pdfLink;
    private final Date insertDate;

    private List<Author> authors;

    public Publication(Integer id, SourceDB sourceDB, String articleId, String mainAuthor, String title, String summary, String doi, String journalRef, String sourceTitle, String sourceVolume, String sourceIssue, String sourcePageRange, Date publicationDate, String pdfLink, Date insertDate) {
        this.id = id;
        this.sourceDbId = null;
        this.sourceDB = sourceDB;
        this.articleId = articleId;
        this.mainAuthor = mainAuthor;
        this.title = title;
        this.summary = summary;
        this.doi = doi;
        this.journalRef = journalRef;
        this.sourceTitle = sourceTitle;
        this.sourceVolume = sourceVolume;
        this.sourceIssue = sourceIssue;
        this.sourcePageRange = sourcePageRange;
        this.publicationDate = publicationDate;
        this.pdfLink = pdfLink;
        this.insertDate = insertDate;
    }

    public Integer getId() {
        return id;
    }

    public String getArticleId() {
        return articleId;
    }

    public Integer getSourceDbId() {
        return sourceDbId;
    }

    public void setSourceDbId(Integer sourceDbId) {
        this.sourceDbId = sourceDbId;
    }

    public String getMainAuthor() {
        return mainAuthor;
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
    
    public String getSourceTitle() {
        return sourceTitle;
    }

    public String getSourceVolume() {
        return sourceVolume;
    }

    public String getSourceIssue() {
        return sourceIssue;
    }

    public String getSourcePageRange() {
        return sourcePageRange;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public String getPdfLink() {
        return pdfLink;
    }

    public Date getInsertDate() {
        return insertDate;
    }

    public SourceDB getSourceDB() {
        return sourceDB;
    }

    public void setSourceDB(SourceDB sourceDB) {
        this.sourceDB = sourceDB;
    }

    public List<Author> getAuthors() throws PublicationWithNoAuthorException {
        if (authors == null || authors.isEmpty()) {
            throw new PublicationWithNoAuthorException();
        } else {
            return authors;
        }
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
            
}
