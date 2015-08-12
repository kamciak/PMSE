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
public class BibTeXGenerator {

    private static final Logger LOGGER = Logger.getLogger(BibTeXGenerator.class);

    /*
     * @Article{Beneke:1997hv,
     author    = "M. Beneke and G. Buchalla and I. Dunietz",
     title     = "{Mixing induced CP asymmetries in inclusive B decays}",
     journal   = "Phys. Lett.",
     volume    = "B393",
     year      = "1997",
     pages     = "132-142",
     eprint        = "hep-ph/9609357"
     }
     */
    public static List<String> generate(List<Publication> publicationList) {
        List<String> bibtexList = new ArrayList<String>();
        for (Publication publication : publicationList) {
            StringBuilder sb = new StringBuilder();

            sb.append(getTypeString());
            sb.append("{");
            sb.append(publication.getArticleId()).append(",\n");
            List<Author> authors;
            try {
                authors = publication.getAuthors();
                sb.append(getAuthorsString(authors)).append("\n");
            } catch (PublicationWithNoAuthorException ex) {
                sb.append(getMainAuthorString(publication)).append("\n");
                LOGGER.error(ex);
            }

            sb.append(getTitleString(publication));
            sb.append(getYearString(publication));
            sb.append(getPagesString(publication));
            sb.append(getEprintString(publication));
            sb.append(getJournalRefString(publication));
            sb.append("}").append("\n\n");

            bibtexList.add(sb.toString());

        }
        return bibtexList;

    }

    private static String getTypeString() {
        return "@Article";
    }

    private static String getAuthorsString(List<Author> authorList) {
        StringBuilder sb = new StringBuilder();
        sb.append("author\t= \"");
        for (int i = 0; i < authorList.size(); ++i) {
            String [] authorDetails = authorList.get(i).getName().split(" ");
            sb.append(authorDetails[0]).append(", ");
            for(int j = 1; j< authorDetails.length; ++j)
            {
                sb.append(authorDetails[j]).append(" ");
            }
            
            if (i < authorList.size() - 1) {
                sb.append("and ");
            }
        }
        sb.append("\",");

        return sb.toString();
    }

    private static String getMainAuthorString(Publication publication) {
        StringBuilder sb = new StringBuilder();
        sb.append("author\t= \"");
        sb.append(publication.getMainAuthor());

        sb.append("\",");

        return sb.toString();
    }

    private static String getTitleString(Publication publication) {
        StringBuilder sb = new StringBuilder();
        sb.append("title\t= \"{");
        sb.append(publication.getTitle());
        sb.append("}\",\n");

        return sb.toString();
    }

    private static String getYearString(Publication publication) {
        Date publicationDate = publication.getPublicationDate();
        if (publicationDate == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("year\t= \"");
        sb.append(DateUtils.formatYearOnly(publicationDate));
        sb.append("\",\n");

        return sb.toString();
    }

    private static String getPagesString(Publication publication) {
        String pageRange = publication.getSourcePageRange();
        if(pageRange == null || pageRange.isEmpty())
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("pages\t= \"");
        sb.append(pageRange);
        sb.append("\",\n");

        return sb.toString();
    }

    private static String getEprintString(Publication publication) {
        String doi = publication.getDoi();
        if(doi == null || doi.isEmpty())
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("eprint\t= \"");
        sb.append(doi);
        sb.append("\",\n");

        return sb.toString();
    }

    private static String getJournalRefString(Publication publication) {
        String journalRef = publication.getJournalRef();
        if(journalRef == null || journalRef.isEmpty())
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("journal\t= \"");
        sb.append(journalRef);
        sb.append("\",\n");

        return sb.toString();
    }
}
