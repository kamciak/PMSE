/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.services.datacollectorservice.wok.parser;

import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.Author;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.CategoryInfo;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.RawRecord;
import com.publicationmetasearchengine.services.datacollectorservice.wok.parser.parts.SourceInfo;
import com.publicationmetasearchengine.utils.DateUtils;
import com.thomsonreuters.wokmws.v3.woksearchlite.LabelValuesPair;
import com.thomsonreuters.wokmws.v3.woksearchlite.LiteRecord;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;

/**
 *
 * @author Kamciak
 */
public class RecordLiteParser {
    private final LiteRecord liteRecord;
    private static final Logger LOGGER = Logger.getLogger(RecordLiteParser.class);
    public RecordLiteParser(LiteRecord record)
    {
        liteRecord = record;
    }
    
    
    public String getId() {
        return liteRecord.getUid();
    }
    
    private String getStringFromList(List<String> stringList)
    {
        String result = "";
        for(String string : stringList)
        {
            result += (string + " ");
        }
        return result;
    }
    
    private String getStringFromLabelValuePairList(List<LabelValuesPair> labelValuesPairList)
    {
        String result = "";
        for(LabelValuesPair labelPair : labelValuesPairList)
        {
            result += (labelPair.getLabel() + " ");
            result += getStringFromList(labelPair.getValue());
        }        
        return result;
    }

    public String getTitle() {
        String title = "";
        List<LabelValuesPair> titlePairs = liteRecord.getTitle();
        for(LabelValuesPair titlePair : titlePairs)
        {
            String titleLabel = titlePair.getLabel();
            String titleValue = getStringFromList(titlePair.getValue());
            if(titleLabel.equals("Title"))
            {
                title = titleValue;
            }
        }
        LOGGER.debug("Sparsowany TITLE: " + title);
        return title;
    }
    
    private String getSourceIssue()
    {
        String sourceIssue = "";
        List<LabelValuesPair> titlePairs = liteRecord.getTitle();
        for(LabelValuesPair titlePair : titlePairs)
        {
            String titleLabel = titlePair.getLabel();
            String titleValue = getStringFromList(titlePair.getValue());
            if(titleLabel.equals("Issue"))
            {
                sourceIssue = titleValue;
            }
        }
        return sourceIssue;
    }
    
    public SourceInfo getSourceInfo() throws ParseException {        
        List<LabelValuesPair> sourcePairList = liteRecord.getSource();
        String sourceTitle = "", sourceVolume = "", sourceBiblioYear = "", sourceBiblioDate = "", sourcePages = "", sourceIssue = "";
        for(LabelValuesPair sourcePair : sourcePairList)
        {
           String sourcePairLabel = sourcePair.getLabel();
           String sourcePairValue = getStringFromList(sourcePair.getValue());
           if(sourcePairLabel.equals("SourceTitle"))
           {
               sourceTitle = sourcePairValue;
           }
           else if(sourcePairLabel.equals("Volume"))
           {
               sourceVolume = sourcePairValue;
           }
           else if(sourcePairLabel.equals("Published.BiblioYear"))
           {
               sourceBiblioYear = sourcePairValue;
           }
           else if(sourcePairLabel.equals("Published.BiblioDate"))
           {
               sourceBiblioDate = sourcePairValue;
           }
           else if(sourcePairLabel.equals("Pages"))
           {
               sourcePages = sourcePairValue;
           }
        }
        LOGGER.debug(String.format("sourceTitle: %s, sourceVolume: %s, sourceBiblioYear: %s, sourceBiblioDate: %s, sourceIssue: %s, sourcePages: %s, sourceTitle: %s",
                sourceTitle, sourceVolume, sourceBiblioYear, sourceBiblioDate, sourceIssue, sourcePages, sourceTitle));

        String publicationStringDate = sourceBiblioDate.trim().toLowerCase() +","+ sourceBiblioYear.trim();
        Date publicationDate = new Date();
        try
        {
            publicationDate = new SimpleDateFormat("MMM d,yyyy", Locale.US).parse(publicationStringDate);
        }
        catch(ParseException e)
        {
            LOGGER.debug(e);
        }
       
//        LOGGER.debug("publicationStringDate: " +publicationStringDate);
        LOGGER.debug("\n KONIEC SOURCEINFO \n");
        return new SourceInfo(sourceTitle, sourceVolume, getSourceIssue(), sourcePages, publicationDate);
    }

    public List<Author> getAuthors() {
        LOGGER.debug("@@@@@@@ getAuthors @@@@@");
        List<Author> authors = new ArrayList<Author>();
        List<LabelValuesPair> authorRecords = liteRecord.getAuthors();
        for (LabelValuesPair authorRecord : authorRecords) {
            if (authorRecord.getLabel().equals("Authors")) {
                for (String author : authorRecord.getValue()) {
                    String[] authorData = author.split(", ");
                    if (authorData.length > 1) {
                        authors.add(new Author(authorData[1], authorData[0]));
                    } else if (authorData.length == 1) {
                        authors.add(new Author("", authorData[0]));
                    } else {
                        continue;
                    }
                }
            }
        }

        LOGGER.debug("===== AUTORZY ====");
        for (Author author : authors) {
            LOGGER.debug(author);
        }
        LOGGER.debug("===== END OF AUTORZY ====");
        return authors;
    }

    public CategoryInfo getCategoryInfo() {
        //
        //WebServiceLite doesn not provide information about category
        //
        return null;
    }

    public String getAbstract() {
        //
        //WebService doesn not provide information about abstract
        //
        //
        String titleAndKeywords = getTitle() + "\n\n";
        titleAndKeywords += "Keywords:\n";

        List<LabelValuesPair> keywordsRecords = liteRecord.getKeywords();
        for (LabelValuesPair keywordRecord : keywordsRecords) {
            String keywordLabel = keywordRecord.getLabel();
            if (keywordLabel.equals("Keywords")) {
                for (String keyword : keywordRecord.getValue()) {
                    titleAndKeywords += (keyword + "\n");
                }
            }
        }
        LOGGER.debug("========== ABSTRACT - keywords =============");
        LOGGER.debug(titleAndKeywords);
        LOGGER.debug("========== END OF ABSTRACT - keywords =============");
        return titleAndKeywords;
    }

    public String getDOI() {
        List<LabelValuesPair> otherRecords = liteRecord.getOther();
        String DOI = "";
        for (LabelValuesPair otherRecord : otherRecords) {
            String otherLabel = otherRecord.getLabel();
            LOGGER.debug("Other:" + otherLabel + "::" + getStringFromList(otherRecord.getValue()));
            if (otherLabel.equals("Identifier.Doi")) {
                for (String doi : otherRecord.getValue()) {
                    DOI += doi;
                }
            }
        }
        LOGGER.debug("Sparsowany DOI: " + DOI);
        return DOI;
    }

    
    public String getJournalRef() throws ParseException {
        return getSourceInfo().getTitle() + ", "
                + "vol. " + getSourceInfo().getVolumeId() + ", "
                + "pages: " + getSourceInfo().getPageRange() + ", "
                + new SimpleDateFormat("yyyy-MM-dd").format(getSourceInfo().getPublicationDate());
    }

        public RawRecord getRecord() throws ParseException {
        return new RawRecord(
                getId(),
                getTitle(),
                getSourceInfo(),
                getAuthors(),
                getCategoryInfo(),
                getAbstract(),
                getDOI(),
                getJournalRef());
    }
}
