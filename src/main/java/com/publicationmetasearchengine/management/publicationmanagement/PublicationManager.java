package com.publicationmetasearchengine.management.publicationmanagement;

import com.publicationmetasearchengine.dao.publications.exceptions.PublicationAlreadyExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.RelationAlreadyExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.RelationDoesNotExistException;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.data.SourceDB;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.data.filters.FilterCriteria;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface PublicationManager {

    Integer insertPublication(int sourceDBId, String articleId, int mainAuthorId,
            String title, String summary, String doi, String journalRef, Integer sourceTitleId,
            String sourceVolume, String sourceIssue, String sourcePageRange,
            Date publicationDate, String pdfLink) throws PublicationAlreadyExistException;

    Date getNewestPublicationDate(int sourceDbId);

    List<Publication> getPublicationsBySourceDBAndDate(SourceDB sourceDB, Date dateFrom);

    List<Publication> getPublicationsMatchingFiltersCriteria(List<FilterCriteria> filtersCritaria);

    List<Publication> getPublicationsMatchingFiltersCriteria(List<FilterCriteria> filtersCritaria, Date afterDate);

    List<Publication> getPublicationOfAuthor(String mainAuthorId);
    
    List<Publication> getAllPublications();
    // ---- user publications ----

    void insertUserPublication(User user, Publication publication) throws RelationAlreadyExistException;

    boolean isUserPublication(User user, Publication publication);

    void removeUserPublication(User user, Publication publication) throws RelationDoesNotExistException;

    void removeUserPublications(User user);
    
    void removeUserSelectedPublications(User user, List<Publication> publicationList);

    Map<Date, List<Publication>> getUserPublications(User user);
}
