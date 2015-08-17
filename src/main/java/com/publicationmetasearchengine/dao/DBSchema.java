package com.publicationmetasearchengine.dao;

import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbFunction;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbJoin;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

public enum DBSchema {
    INSTANCE;

    public static DbSpec DBSPEC = new DbSpec();
    public static DbSchema DBSCHEMA = DBSPEC.addDefaultSchema();

    public static DbTable USERS_TABLE = DBSCHEMA.addTable("user");
    public static DbColumn USERS_ID_COLUMN = USERS_TABLE.addColumn("id");
    public static DbColumn USERS_LOGIN_COLUMN = USERS_TABLE.addColumn("login");
    public static DbColumn USERS_PASSWORD_COLUMN = USERS_TABLE.addColumn("passwd");
    public static DbColumn USERS_NAME_COLUMN = USERS_TABLE.addColumn("name");
    public static DbColumn USERS_SURNAME_COLUMN = USERS_TABLE.addColumn("surname");
    public static DbColumn USERS_EMAIL_COLUMN = USERS_TABLE.addColumn("email");

    public static DbTable PUBLICATION_TABLE = DBSCHEMA.addTable("publication");
    public static DbColumn PUBLICATION_ID_COLUMN = PUBLICATION_TABLE.addColumn("id");
    public static DbColumn PUBLICATION_SOURCEDB_ID_COLUMN = PUBLICATION_TABLE.addColumn("sourceDBId");
    public static DbColumn PUBLICATION_ARTICLE_ID_COLUMN = PUBLICATION_TABLE.addColumn("articleId");
    public static DbColumn PUBLICATION_MAIN_AUTHOR_ID_COLUMN = PUBLICATION_TABLE.addColumn("mainAuthorId");
    public static DbColumn PUBLICATION_TITLE_COLUMN = PUBLICATION_TABLE.addColumn("title");
    public static DbColumn PUBLICATION_SUMMARY_COLUMN = PUBLICATION_TABLE.addColumn("summary");
    public static DbColumn PUBLICATION_DOI_COLUMN = PUBLICATION_TABLE.addColumn("doi");
    public static DbColumn PUBLICATION_JOURNAL_REF_COLUMN = PUBLICATION_TABLE.addColumn("journalRef");
    public static DbColumn PUBLICATION_SOURCE_TITLE_ID_COLUMN = PUBLICATION_TABLE.addColumn("sourceTitleId");
    public static DbColumn PUBLICATION_SOURCE_VOLUME_COLUMN = PUBLICATION_TABLE.addColumn("sourceVolume");
    public static DbColumn PUBLICATION_SOURCE_ISSUE_COLUMN = PUBLICATION_TABLE.addColumn("sourceIssue");
    public static DbColumn PUBLICATION_SOURCE_PAGERANGE_COLUMN = PUBLICATION_TABLE.addColumn("sourcePageRange");
    public static DbColumn PUBLICATION_PUBLICATION_DATE_COLUMN = PUBLICATION_TABLE.addColumn("publicationDate");
    public static DbColumn PUBLICATION_PDF_LINK_COLUMN = PUBLICATION_TABLE.addColumn("pdfLink");
    public static DbColumn PUBLICATION_INSERT_DATE_COLUMN = PUBLICATION_TABLE.addColumn("insertDate");
    public static DbColumn PUBLICATION_MARK_TO_READ_COUNTER = PUBLICATION_TABLE.addColumn("markToReadCounter");

    public static DbTable AUTHOR_TABLE = DBSCHEMA.addTable("author");
    public static DbColumn AUTHOR_ID_COLUMN = AUTHOR_TABLE.addColumn("id");
    public static DbColumn AUTHOR_NAME_COLUMN = AUTHOR_TABLE.addColumn("name");

    public static DbTable PUBLICATIONAUTHORS_TABLE = DBSCHEMA.addTable("publicationAuthors");
    public static DbColumn PUBLICATIONAUTHORS_PUBLICATION_ID_COLUMN = PUBLICATIONAUTHORS_TABLE.addColumn("publicationId");
    public static DbColumn PUBLICATIONAUTHORS_AUTHOR_ID_COLUMN = PUBLICATIONAUTHORS_TABLE.addColumn("authorId");

    public static DbTable SOURCETITLE_TABLE = DBSCHEMA.addTable("sourceTitle");
    public static DbColumn SOURCETITLE_ID_COLUMN = SOURCETITLE_TABLE.addColumn("id");
    public static DbColumn SOURCETITLE_TITLE_COLUMN = SOURCETITLE_TABLE.addColumn("title");

    public static DbTable SOURCEDB_TABLE = DBSCHEMA.addTable("sourceDb");
    public static DbColumn SOURCEDB_ID_COLUMN = SOURCEDB_TABLE.addColumn("id");
    public static DbColumn SOURCEDB_FULLNAME_COLUMN = SOURCEDB_TABLE.addColumn("fullName");
    public static DbColumn SOURCEDB_SHORTNAME_COLUMN = SOURCEDB_TABLE.addColumn("shortName");

    public static DbTable USERPUBLICATIONS_TABLE = DBSCHEMA.addTable("userPublications");
    public static DbColumn USERPUBLICATIONS_USER_ID_COLUMN = USERPUBLICATIONS_TABLE.addColumn("IdU");
    public static DbColumn USERPUBLICATIONS_PUBLICATION_ID_COLUMN = USERPUBLICATIONS_TABLE.addColumn("IdP");
    public static DbColumn USERPUBLICATIONS_INSERT_DATE_COLUMN = USERPUBLICATIONS_TABLE.addColumn("insertDate");

    public static DbTable FILTERCRITERIAS_TABLE = DBSCHEMA.addTable("filterCriterias");
    public static DbColumn FILTERCRITERIAS_ID_COLUMN = FILTERCRITERIAS_TABLE.addColumn("IdF");
    public static DbColumn FILTERCRITERIAS_USER_ID_COLUMN = FILTERCRITERIAS_TABLE.addColumn("IdU");
    public static DbColumn FILTERCRITERIAS_FILTERS_COLUMN = FILTERCRITERIAS_TABLE.addColumn("filters");
    public static DbColumn FILTERCRITERIAS_LAST_SEARCH_DATE_COLUMN = FILTERCRITERIAS_TABLE.addColumn("lastSearchDate");
    
    public static DbTable IMPACTFACTOR_TABLE = DBSCHEMA.addTable("impactFactor");
    public static DbColumn IMPACTFACTOR_ID_COLUMN = IMPACTFACTOR_TABLE.addColumn("id");
    public static DbColumn IMPACTFACTOR_NAME_COLUMN = IMPACTFACTOR_TABLE.addColumn("journal");
    public static DbColumn IMPACTFACTOR_ISSN_COLUMN = IMPACTFACTOR_TABLE.addColumn("ISSN");
    public static DbColumn IMPACTFACTOR_2013_2014_COLUMN = IMPACTFACTOR_TABLE.addColumn("if2013_2014");
    public static DbColumn IMPACTFACTOR_2012_COLUMN = IMPACTFACTOR_TABLE.addColumn("if2012");
    public static DbColumn IMPACTFACTOR_2011_COLUMN = IMPACTFACTOR_TABLE.addColumn("if2011");
    public static DbColumn IMPACTFACTOR_2010_COLUMN = IMPACTFACTOR_TABLE.addColumn("if2010");
    public static DbColumn IMPACTFACTOR_2009_COLUMN = IMPACTFACTOR_TABLE.addColumn("if2009");
    public static DbColumn IMPACTFACTOR_2008_COLUMN = IMPACTFACTOR_TABLE.addColumn("if2008");

    //JOINs
    public static DbJoin PUBLICATION_MAINAUTHOR_JOIN = DBSPEC.addJoin(
            null, PUBLICATION_TABLE.getName(),
            null, AUTHOR_TABLE.getName(),
            new String[]{PUBLICATION_MAIN_AUTHOR_ID_COLUMN.getName()}, new String[]{AUTHOR_ID_COLUMN.getName()});

    public static DbJoin PUBLICATION_SOURCETITLE_JOIN = DBSPEC.addJoin(
            null, PUBLICATION_TABLE.getName(),
            null, SOURCETITLE_TABLE.getName(),
            new String[]{PUBLICATION_SOURCE_TITLE_ID_COLUMN.getName()}, new String[]{SOURCETITLE_ID_COLUMN.getName()});

    public static DbJoin PUBLICATIONAUTHORS_AUTHOR_JOIN = DBSPEC.addJoin(
            null, PUBLICATIONAUTHORS_TABLE.getName(),
            null, AUTHOR_TABLE.getName(),
            new String[]{PUBLICATIONAUTHORS_AUTHOR_ID_COLUMN.getName()}, new String[]{AUTHOR_ID_COLUMN.getName()});

    public static DbJoin AUTHOR_PUBLICATIONAUTHORS_JOIN = DBSPEC.addJoin(
            null, AUTHOR_TABLE.getName(),
            null, PUBLICATIONAUTHORS_TABLE.getName(),
            new String[]{AUTHOR_ID_COLUMN.getName()}, new String[]{PUBLICATIONAUTHORS_AUTHOR_ID_COLUMN.getName()});

    //Functions
    public static final DbFunction FUNCTION_UPPER = new DbFunction(null, "UPPER");
    public static final DbFunction FUNCTION_MAX = new DbFunction(null, "MAX");

    public static FunctionCall getFunctionCall(DbFunction function, DbColumn column){
        return new FunctionCall(function).addColumnParams(column);
    }
}
