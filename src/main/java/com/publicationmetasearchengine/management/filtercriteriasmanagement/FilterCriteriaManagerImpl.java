package com.publicationmetasearchengine.management.filtercriteriasmanagement;

import com.publicationmetasearchengine.dao.filtercriterias.FilterCriteriaDAO;
import com.publicationmetasearchengine.dao.filtercriterias.exceptions.FilterCriteriasDoesNotExistException;
import com.publicationmetasearchengine.dao.users.UserDAO;
import com.publicationmetasearchengine.dao.users.exceptions.UserDoesNotExistException;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.data.filters.NamedFilterCriteria;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

@Component
@Configurable(preConstruction = true)
public class FilterCriteriaManagerImpl implements FilterCriteriaManager {
    private static final Logger LOGGER = Logger.getLogger(FilterCriteriaManagerImpl.class);

    @Autowired
    FilterCriteriaDAO filterCriteriaDAO;
    @Autowired
    UserDAO userDAO;

    @Override
    public int saveNewFilterCriterias(User user, NamedFilterCriteria filterCriteria) throws UserDoesNotExistException {
        userDAO.getUserById(user.getId());
        int result = filterCriteriaDAO.addFilterCriteriasForUser(filterCriteria, user.getId());
        LOGGER.info(String.format("Filter criterias [%s] added for user %s under ID = %d", filterCriteria.toString(), user, result));
        return result;
    }

    @Override
    public void saveModifiedFilterCriterias(User user, NamedFilterCriteria newFilterCriteria) throws UserDoesNotExistException, FilterCriteriasDoesNotExistException {
        userDAO.getUserById(user.getId());
        filterCriteriaDAO.modifyFilterCriteriasById(newFilterCriteria, newFilterCriteria.getId());
        LOGGER.info(String.format("Filter criterias [%s] modified by user %s", newFilterCriteria.toString(), user));
    }

    @Override
    public List<NamedFilterCriteria> getUsersFilterCriterias(User user) throws UserDoesNotExistException {
        userDAO.getUserById(user.getId());
        final List<NamedFilterCriteria> filterCriterias = filterCriteriaDAO.getAllFiltersCriteriasByUserId(user.getId());
        LOGGER.debug(String.format("Found %d criterias for user %s", filterCriterias.size(), user));
        return filterCriterias;
    }

    @Override
    public void deleteFilterCriterias(User user, int filterCriteriasId) throws UserDoesNotExistException, FilterCriteriasDoesNotExistException {
        userDAO.getUserById(user.getId());
        filterCriteriaDAO.deleteFilterCriteriasByID(filterCriteriasId);
        LOGGER.info(String.format("Filter criteria with id = %d removed by user %s", filterCriteriasId, user));
    }
}
