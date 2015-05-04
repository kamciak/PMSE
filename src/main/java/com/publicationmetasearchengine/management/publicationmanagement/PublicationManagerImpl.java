package com.publicationmetasearchengine.management.publicationmanagement;

import com.publicationmetasearchengine.dao.publications.PublicationDAO;
import com.publicationmetasearchengine.dao.publications.exceptions.PublicationAlreadyExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.RelationAlreadyExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.RelationDoesNotExistException;
import com.publicationmetasearchengine.dao.sourcedbs.SourceDbDAO;
import com.publicationmetasearchengine.dao.sourcedbs.exceptions.SourceDbDoesNotExistException;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.data.SourceDB;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.data.filters.FilterCriteria;
import com.publicationmetasearchengine.management.authormanagement.AuthorManager;
import com.publicationmetasearchengine.utils.DateUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

@Component
@Configurable(preConstruction = true)
public class PublicationManagerImpl implements PublicationManager {
    private static final Logger LOGGER = Logger.getLogger(PublicationManagerImpl.class);

    @Autowired
    PublicationDAO publicationDAO;
    @Autowired
    AuthorManager authorManager;
    @Autowired
    SourceDbDAO sourceDbDAO;


    @Override
    public Integer insertPublication(int sourceDBId, String articleId, int mainAuthorId,
            String title, String summary, String doi, String journalRef, Integer sourceTitleId,
            String sourceVolume, String sourceIssue, String sourcePageRange,
            Date publicationDate, String pdfLink) throws PublicationAlreadyExistException {
        Integer result = publicationDAO.insertPublication(sourceDBId, articleId,
                mainAuthorId, title, summary, doi, journalRef, sourceTitleId, sourceVolume,
                sourceIssue, sourcePageRange, publicationDate, pdfLink);
        LOGGER.info(String.format("Publication [%d-%s] added to database under ID: %d", sourceDBId, articleId, result));
        return result;
    }
    
    @Override
    public List<Publication> getAllPublications(){
        List<Publication> publications = publicationDAO.getAllPublications();
        for(Publication publication: publications)
            try {
            publication.setSourceDB(sourceDbDAO.getSourceDBById(publication.getSourceDbId()));
        } catch (SourceDbDoesNotExistException ex) {
            
        }
        return publications;
    }
    
    @Override
    public List<Publication> getPublicationsBySourceDBAndDate(SourceDB sourceDB, Date dateFrom) {
        List<Publication> publications = publicationDAO.getPublicationsBySourceDbIdDate(sourceDB.getId(), dateFrom);
        for (Publication publication : publications)
            try {
                publication.setSourceDB(sourceDbDAO.getSourceDBById(publication.getSourceDbId()));
            } catch (SourceDbDoesNotExistException ex) {
                LOGGER.error("Should not occure!!", ex);
            }
        LOGGER.debug(String.format("Found %d publications for SourceDB: %s newer than %s", publications.size(), sourceDB.getShortName(), DateUtils.formatDateOnly(dateFrom)));
        return publications;
    }
    
    @Override
    public List<Publication> getPublicationOfAuthor(String mainAuthorId){
        List<Publication> publications = publicationDAO.getPublicationOfAuthor(mainAuthorId);
        for (Publication publication : publications)
            try {
                publication.setSourceDB(sourceDbDAO.getSourceDBById(publication.getSourceDbId()));
            } catch (SourceDbDoesNotExistException ex) {
                LOGGER.error("Should not occure!!", ex);
            }
        LOGGER.debug(String.format("Found %d publications of author", publications.size()));
        return publications;
    }

    @Override
    public List<Publication> getPublicationsMatchingFiltersCriteria(List<FilterCriteria> filtersCritaria) {
        LOGGER.debug(String.format("Executing query for filters [%s]", filtersCritaria.toString()));
        List<Publication> publications = publicationDAO.getPublicationsByFiltersCritaria(filtersCritaria, null);
        for (Publication publication : publications)
            try {
                publication.setSourceDB(sourceDbDAO.getSourceDBById(publication.getSourceDbId()));
            } catch (SourceDbDoesNotExistException ex) {
                LOGGER.error("Should not occure!!", ex);
            }
        LOGGER.debug(String.format("Found %d publications", publications.size()));
        return publications;
    }

    // for notification service
    @Override
    public List<Publication> getPublicationsMatchingFiltersCriteria(List<FilterCriteria> filtersCritaria, Date afterDate) {
        LOGGER.debug(String.format("Executing query for filters [%s] after date %s", filtersCritaria.toString(), afterDate.toString()));
        List<Publication> publications = publicationDAO.getPublicationsByFiltersCritaria(filtersCritaria, afterDate);
        LOGGER.debug(String.format("Found %d publications", publications.size()));
        return publications;
    }

    @Override
    public void insertUserPublication(User user, Publication publication) throws RelationAlreadyExistException {
        publicationDAO.insertUserPublication(user.getId(), publication.getId());
        LOGGER.info(String.format("Relation [%s] - [%d-%s] added to database", user, publication.getSourceDbId(), publication.getArticleId()));
    }

    @Override
    public boolean isUserPublication(User user, Publication publication) {
        return publicationDAO.checkUserPublication(user.getId(), publication.getId());
    }



    @Override
    public void removeUserPublication(User user, Publication publication) throws RelationDoesNotExistException {
        publicationDAO.removeUserPublication(user.getId(), publication.getId());
        LOGGER.info(String.format("Relation %s - [%d-%s] removed from database", user, publication.getSourceDbId(), publication.getArticleId()));
    }

    @Override
    public void removeUserPublications(User user) {
        try {
            publicationDAO.removeUserPublications(user.getId());
            LOGGER.info(String.format("All users [%s] publications removed", user));
        } catch (RelationDoesNotExistException ex) {
        }
    }

    @Override
    public void removeUserSelectedPublications(User user, List<Publication> publicationList){
        try {
            for (Publication publication : publicationList){
                publicationDAO.removeUserPublication(user.getId(), publication.getId());
            }
        } catch(RelationDoesNotExistException ex){}
    }


    @Override
    @SuppressWarnings("unchecked")
    public Map<Date, List<Publication>> getUserPublications(User user) {
        final Map<Integer, Date> userPublicationsIds = publicationDAO.getUserPublicationsIds(user.getId());

        Map<Date, List<Publication>> result = new HashMap<Date, List<Publication>>();
        if (userPublicationsIds.isEmpty())
            return result;
        final List<Publication> publicationsByIds = publicationDAO.getPublicationsByIds(new ArrayList<Integer>(userPublicationsIds.keySet()));
        for (Publication publication : publicationsByIds) {
            try {
                publication.setSourceDB(sourceDbDAO.getSourceDBById(publication.getSourceDbId()));
            } catch (SourceDbDoesNotExistException ex) {
            }
            Date date = userPublicationsIds.get(publication.getId());
            if (result.get(date)== null)
                result.put(date, new ArrayList<Publication>());
            result.get(date).add(publication);
        }

        return result;
    }

    @Override
    public Date getNewestPublicationDate(int sourceDbId) {
        Date date = publicationDAO.getNewestPublicationDateBySourceDBId(sourceDbId);
        LOGGER.debug(String.format("Newest date for sourceDb: %d is %s", sourceDbId, date));
        return date;
    }
}
