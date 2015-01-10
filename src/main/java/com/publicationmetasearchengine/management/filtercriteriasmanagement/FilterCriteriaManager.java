package com.publicationmetasearchengine.management.filtercriteriasmanagement;

import com.publicationmetasearchengine.dao.filtercriterias.exceptions.FilterCriteriasDoesNotExistException;
import com.publicationmetasearchengine.dao.users.exceptions.UserDoesNotExistException;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.data.filters.NamedFilterCriteria;
import java.util.List;

public interface FilterCriteriaManager {

    int saveNewFilterCriterias(User user, NamedFilterCriteria filterCriteria) throws UserDoesNotExistException;

    //Remember to set criteria id
    void saveModifiedFilterCriterias(User user, NamedFilterCriteria newFilterCriteria) throws UserDoesNotExistException, FilterCriteriasDoesNotExistException;

    List<NamedFilterCriteria> getUsersFilterCriterias(User user) throws UserDoesNotExistException;

    void deleteFilterCriterias(User user, int filterCriteriasId) throws UserDoesNotExistException, FilterCriteriasDoesNotExistException;
}
