/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.utils;

import com.publicationmetasearchengine.dao.publications.exceptions.PublicationWithNoAuthorException;
import com.publicationmetasearchengine.data.Author;
import com.publicationmetasearchengine.data.Publication;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Kamciak
 */

public class BibliographyGenerator {
    private static final Logger LOGGER = Logger.getLogger(BibliographyGenerator.class);
    
    public static List<String> generate(List<Publication> publicationList){
        
        
        
        List<String> bibliographyList = new ArrayList<String>();
        for(Publication publication : publicationList)
        {
            StringBuilder sb = new StringBuilder();
            List<Author> authors;
            try {
                authors = publication.getAuthors();
                sb.append(getAuthorsString(authors));
            } catch (PublicationWithNoAuthorException ex) {
                sb.append(getMainAuthorString(publication));
                LOGGER.error(ex);
            }
            
            sb.append(getTitleString(publication));
            sb.append(getJournalRefString(publication));
            sb.append(getYearString(publication));
            sb.append("\n\n");
            bibliographyList.add(sb.toString());
        }
        
        
        return bibliographyList;
    }
    
        private static String getMainAuthorString(Publication publication) {
        StringBuilder sb = new StringBuilder();
        sb.append(publication.getMainAuthor());

        sb.append(", ");

        return sb.toString();
    }
    
    private static String getAuthorsString(List<Author> authorList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < authorList.size(); ++i) {
            sb.append(authorList.get(i).getName());
                if (i == authorList.size() - 2) {
                    sb.append(" and ");
                    sb.append(authorList.get(i + 1).getName());
                    sb.append(", ");
                    break;
                } else {
                    sb.append(", ");
                }
        }
        return sb.toString();
    }
    
        public static String getTitleString(Publication publication) {
        StringBuilder sb = new StringBuilder();
        sb.append(publication.getTitle());
        sb.append(", ");

        return sb.toString();
    }
        
        
    private static String getJournalRefString(Publication publication) {
        String journalRef = publication.getJournalRef();
        if(journalRef == null || journalRef.isEmpty())
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(journalRef);
        sb.append(", ");

        return sb.toString();
    }
    
        private static String getYearString(Publication publication) {
        Date publicationDate = publication.getPublicationDate();
        if (publicationDate == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(DateUtils.formatYearOnly(publicationDate));
        sb.append(")");

        return sb.toString();
    }
}
