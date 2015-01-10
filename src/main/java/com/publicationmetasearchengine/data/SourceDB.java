package com.publicationmetasearchengine.data;

public class SourceDB {
    private final int id;
    private final String fullName;
    private final String shortName;

    public SourceDB(int id, String fullName, String shortName) {
        this.id = id;
        this.fullName = fullName;
        this.shortName = shortName;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getShortName() {
        return shortName;
    }
}
