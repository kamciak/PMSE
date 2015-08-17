/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.utils.impactfactor;

import com.publicationmetasearchengine.dao.impactfactor.ImpactFactorDAO;
import com.publicationmetasearchengine.data.Journal;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author Kamciak
 */
@Configurable(preConstruction = true)
public class ImpactFactorMapper {
    private static HashMap<String, List<Journal>> journalsHashMapStartsAt;
    private static HashMap<String, Float[]> resultMapImpactFactor;
    @Autowired
    ImpactFactorDAO impactFactorDao;
    
    public ImpactFactorMapper()
    {
        journalsHashMapStartsAt = new HashMap<String, List<Journal>>();
        resultMapImpactFactor = new HashMap<String, Float[]>();
    }
    
    public List<Journal> getImpactFactorMapForLetter(char firstLetter)
    {
        String firstLetterString = String.valueOf(firstLetter);
        if (!journalsHashMapStartsAt.keySet().contains(firstLetterString)) {
                    journalsHashMapStartsAt.put(firstLetterString, impactFactorDao.getJournalsStartAt(firstLetter));
        }
        
        return journalsHashMapStartsAt.get(String.valueOf(firstLetterString));
    }
    
    public Float[] getImpactFactorForJournal(String journalTitle) {
        if (journalTitle != null && !journalTitle.isEmpty()) {
            if (resultMapImpactFactor.containsKey(journalTitle)) {
                return resultMapImpactFactor.get(journalTitle);
            } else {
                List<Journal> journalList = getImpactFactorMapForLetter(journalTitle.charAt(0));
                for (int i = 0; i < journalList.size(); ++i) {
                    if (Pattern.compile(Pattern.quote(journalList.get(i).getTitle()), Pattern.CASE_INSENSITIVE).matcher(journalTitle).find()) {
                        journalTitle = journalList.get(i).getTitle();
                        Float[] impactFactors = {journalList.get(i).getImpactFactor2013_2014(), calculateAverage(journalList.get(i))};
                        resultMapImpactFactor.put(journalTitle, impactFactors);

                        return impactFactors;
                    }
                }
            }
        }
        Float[] result = {0.0f, 0.0f};
        return result;
    }
    
    private Float calculateAverage(Journal journal) {
        Float ifSum = 0.0f;

        Float[] impactFactors = {journal.getImpactFactor2008(),
            journal.getImpactFactor2009(),
            journal.getImpactFactor2010(),
            journal.getImpactFactor2011(),
            journal.getImpactFactor2012(),
            journal.getImpactFactor2013_2014()};

        int counter = 0;
        for (int i = 0; i < impactFactors.length; ++i) {
            if (impactFactors[i] != null) {
                ifSum += impactFactors[i];
                ++counter;
            }
        }

        return ifSum / counter;
    }

}
