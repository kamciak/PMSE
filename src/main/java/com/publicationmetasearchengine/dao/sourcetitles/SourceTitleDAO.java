package com.publicationmetasearchengine.dao.sourcetitles;

import com.publicationmetasearchengine.dao.sourcetitles.exceptions.SourceTitleAlreadyExists;
import com.publicationmetasearchengine.dao.sourcetitles.exceptions.SourceTitleDoesNotExists;

public interface SourceTitleDAO {

    Integer addTitle(String title) throws SourceTitleAlreadyExists;

    Integer getTitleIdByTitle(String title) throws SourceTitleDoesNotExists;

}
