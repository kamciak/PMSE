package com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts;

import java.text.ParseException;
import java.util.Date;

public class SourceInfo {

    private final String title;
    private final String volumeId;
    private final String issueId;
    private final String pageRange;
    private final Date publicationDate;

    public SourceInfo(String title, String volumeId, String issueId, String pageRange, Date publicationDate) throws ParseException {
        this.title = title;
        this.volumeId = volumeId;
        this.issueId = issueId;
        this.pageRange = pageRange;
        this.publicationDate = publicationDate;
    }

    public String getTitle() {
        return title;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public String getIssueId() {
        return issueId;
    }

    public String getPageRange() {
        return pageRange;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }
}
