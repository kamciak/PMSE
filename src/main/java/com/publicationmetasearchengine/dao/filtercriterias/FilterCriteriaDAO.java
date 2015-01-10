package com.publicationmetasearchengine.dao.filtercriterias;

import com.publicationmetasearchengine.dao.filtercriterias.exceptions.FilterCriteriasDoesNotExistException;
import com.publicationmetasearchengine.data.filters.NamedFilterCriteria;
import java.util.List;

public interface FilterCriteriaDAO {

    int addFilterCriteriasForUser(NamedFilterCriteria filterCriteria, int userId);

    void modifyFilterCriteriasById(NamedFilterCriteria newFilterCriteria, int filterCriteriaId) throws FilterCriteriasDoesNotExistException;

    void deleteFilterCriteriasByID(int filterCriteriaId) throws FilterCriteriasDoesNotExistException;

    List<NamedFilterCriteria> getAllFiltersCriterias();

    List<NamedFilterCriteria> getAllFiltersCriteriasByUserId(int userId);

    void touchFilterCriteriasById(int filterCriteriaId) throws FilterCriteriasDoesNotExistException;
}
