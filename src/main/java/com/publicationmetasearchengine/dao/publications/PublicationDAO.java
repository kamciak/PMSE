package com.publicationmetasearchengine.dao.publications;

import com.publicationmetasearchengine.dao.publications.exceptions.PublicationAlreadyExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.PublicationDoesNotExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.RelationAlreadyExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.RelationDoesNotExistException;
import com.publicationmetasearchengine.data.Author;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.data.filters.FilterCriteria;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface PublicationDAO {

    Integer insertPublication(int sourceDBId, String articleId, int mainAuthorId,
            String title, String summary, String doi, String journalRef, Integer sourceTitleId,
            String sourceVolume, String sourceIssue, String sourcePageRange,
            Date publicationDate, String pdfLink) throws PublicationAlreadyExistException;

    Publication getPublication(int sourceDbId, String articleId) throws PublicationDoesNotExistException;
    
    Publication getPublication(int id) throws PublicationDoesNotExistException;

    List<Publication> getPublicationsBySourceDbIdDate(int sourceDbId, Date date);

    List<Publication> getPublicationsByIds(List<Integer> ids);

    List<Publication> getPublicationsByFiltersCritaria(List<FilterCriteria> filtersCritaria, Date afterDate);

    List<Publication> getPublicationOfAuthor(String mainAuthorId);
    
    List<Publication> getAllPublications();
    
    Date getNewestPublicationDateBySourceDBId(int sourceDbId);

    // ---- user publications ----

    void insertUserPublication(int userId, int publicationId) throws RelationAlreadyExistException;

    boolean checkUserPublication(int userId, int publicationId);

    void removeUserPublication(int userId, int publicationId) throws RelationDoesNotExistException;

    void removeUserPublications(int userId) throws RelationDoesNotExistException;

    Map<Integer, Date> getUserPublicationsIds(int userId);
    
    Publication getPublicationByArticleId(String articleId) throws PublicationDoesNotExistException;
}
