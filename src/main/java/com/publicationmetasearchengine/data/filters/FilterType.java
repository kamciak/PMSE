package com.publicationmetasearchengine.data.filters;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.publicationmetasearchengine.dao.DBSchema;

public enum FilterType {
    TITLE(DBSchema.PUBLICATION_TITLE_COLUMN),
    SUMMARY(DBSchema.PUBLICATION_SUMMARY_COLUMN),
    PUBLICATION_DATE(DBSchema.PUBLICATION_PUBLICATION_DATE_COLUMN),
    AUTHOR(null),
    DOI(DBSchema.PUBLICATION_DOI_COLUMN),
    JOURNAL(DBSchema.PUBLICATION_JOURNAL_REF_COLUMN);

    private final DbColumn dbColumn;

    private FilterType(DbColumn dbColumn) {
        this.dbColumn = dbColumn;
    }

    public DbColumn getDbColumn() {
        return dbColumn;
    }
}
