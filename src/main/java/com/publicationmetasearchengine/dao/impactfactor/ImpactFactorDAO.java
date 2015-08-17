/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.dao.impactfactor;

import com.publicationmetasearchengine.dao.impactfactor.exceptions.JournalAlreadyExistException;
import com.publicationmetasearchengine.dao.impactfactor.exceptions.JournalDoesNotExistException;
import com.publicationmetasearchengine.data.Journal;
import java.util.List;

/**
 *
 * @author Kamciak
 */
public interface ImpactFactorDAO {
    Integer insertImpactFactor(Journal journal) throws JournalAlreadyExistException;
    
    Journal getJournalById(int journalId) throws JournalDoesNotExistException;
    
    Journal getJournalByISSN(String ISSN) throws JournalDoesNotExistException;
    
    List<Journal> getAllJournals();
    
    List<Journal> getJournalsStartAt(char firstChar);
}
