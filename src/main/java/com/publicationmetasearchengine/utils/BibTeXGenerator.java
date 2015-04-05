/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.utils;

import com.publicationmetasearchengine.dao.publications.exceptions.PublicationWithNoAuthorException;
import com.publicationmetasearchengine.data.Author;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.gui.searchscreen.SearchScreenPanel;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Kamciak
 */
public class BibTeXGenerator {

    private static final Logger LOGGER = Logger.getLogger(SearchScreenPanel.class);

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

            sb.append(getTitleString(publication)).append("\n");
            sb.append(getYearString(publication)).append("\n");
            sb.append(getPagesString(publication)).append("\n");
            sb.append(getEprintString(publication)).append("\n");
            sb.append(getJournalRefString(publication)).append("\n");

            sb.append("}").append("\n\n");

            bibtexList.add(sb.toString());

        }
        return bibtexList;

    }

    public static String getTypeString() {
        return "@Article";
    }

    public static String getAuthorsString(List<Author> authorList) {
        StringBuilder sb = new StringBuilder();
        sb.append("author\t= \"");
        for (int i = 0; i < authorList.size(); ++i) {
            sb.append(authorList.get(i).getName());
            if (i < authorList.size() - 1) {
                sb.append(" and ");
            }
        }
        sb.append("\",");

        return sb.toString();
    }

    public static String getMainAuthorString(Publication publication) {
        StringBuilder sb = new StringBuilder();
        sb.append("author\t= \"");
        sb.append(publication.getMainAuthor());

        sb.append("\",");

        return sb.toString();
    }

    public static String getTitleString(Publication publication) {
        StringBuilder sb = new StringBuilder();
        sb.append("title\t= \"{");
        sb.append(publication.getTitle());
        sb.append("}\",");

        return sb.toString();
    }

    public static String getYearString(Publication publication) {
        StringBuilder sb = new StringBuilder();
        sb.append("year\t= \"");
        sb.append(DateUtils.formatYearOnly(publication.getPublicationDate()));
        sb.append("\",");

        return sb.toString();
    }

    public static String getPagesString(Publication publication) {
        StringBuilder sb = new StringBuilder();
        sb.append("pages\t= \"");
        sb.append(publication.getSourcePageRange());
        sb.append("\",");

        return sb.toString();
    }

    public static String getEprintString(Publication publication) {
        StringBuilder sb = new StringBuilder();
        sb.append("eprint\t= \"");
        //do zmiany;
        sb.append(publication.getDoi());
        sb.append("\",");

        return sb.toString();
    }

    public static String getJournalRefString(Publication publication) {
        StringBuilder sb = new StringBuilder();
        sb.append("journal\t= \"");
        sb.append(publication.getJournalRef());
        sb.append("\",");

        return sb.toString();
    }
}
