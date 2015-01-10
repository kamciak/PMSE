package com.publicationmetasearchengine.dao.sourcedbs;

import com.publicationmetasearchengine.dao.sourcedbs.exceptions.SourceDbDoesNotExistException;
import com.publicationmetasearchengine.data.SourceDB;
import java.util.List;

public interface SourceDbDAO {
    static final SourceDB ALL_DB_SOURCE = new SourceDB(-1, "All databases", "All");

    int getSourceIdByShortName(String shortName) throws SourceDbDoesNotExistException;

    SourceDB getSourceDBById(int sourceId) throws SourceDbDoesNotExistException;

    List<SourceDB> getAllSourceDBS();
}
