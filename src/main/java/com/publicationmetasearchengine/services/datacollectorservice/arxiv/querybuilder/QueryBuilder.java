package com.publicationmetasearchengine.services.datacollectorservice.arxiv.querybuilder;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class QueryBuilder {
    private static final Logger LOGGER = Logger.getLogger(QueryBuilder.class);

    private static final String API_QUERY_PATTERN = "http://export.arxiv.org/api/query?search_query=cat:%s&start=%d&max_results=%d&sortBy=submittedDate&sortOrder=%s";
    private static final String API_AUTHOR_QUERY_PATTERN = "http://export.arxiv.org/api/query?search_query=au:%s&start=%d&max_results=%d&sortBy=submittedDate&sortOrder=%s";
    private static final String API_JOURNAL_SINGLE_QUERY_PATTERN = "http://export.arxiv.org/api/query?search_query=%s&start=%d&max_results=%d&sortBy=submittedDate&sortOrder=%s";
    private static final String API_JOURNAL_QUERY_PATTERN = "http://export.arxiv.org/api/query?search_query=%s+%s+%s&start=%d&max_results=%d&sortBy=submittedDate&sortOrder=%s";

    private String searchString;
    private List<String> titleKeys;
    private List<String> abstractKeys;
    private final int startPos;
    private final int maxResults;
    private final boolean ascending;
    private ComboCondition.Op titleKeysInnerOperator;
    private ComboCondition.Op outerOperator;
    private ComboCondition.Op abstractKeysInnerOperator;

    public QueryBuilder(String searchString, int startPos, int maxResults, boolean ascending) {
        this.searchString = searchString;
        this.startPos = startPos;
        this.maxResults = maxResults;
        this.ascending = ascending;

    }
    
    public QueryBuilder(List<String> titleKeys, 
                        ComboCondition.Op titleKeysInnerOperator, 
                        ComboCondition.Op outerOperator, 
                        List<String> abstractKeys,
                        ComboCondition.Op abstractKeysInnerOperator,
                        int startPos,
                        int maxResults,
                        boolean ascending) {
 
        this.abstractKeys = abstractKeys;
        this.startPos = startPos;
        this.maxResults = maxResults;
        this.ascending = ascending;
        this.titleKeys = titleKeys;
        this.abstractKeysInnerOperator = abstractKeysInnerOperator;
        this.outerOperator = outerOperator;
        this.titleKeysInnerOperator = titleKeysInnerOperator;
    }

    public String build(){
        final String queryLink = String.format(API_QUERY_PATTERN, searchString, startPos, maxResults, ascending?"ascending":"descending");
        LOGGER.debug("Builded query link: " + queryLink);
        return queryLink;
    }
    
    public String buildForAuthor()
    {
        final String queryLink = String.format(API_AUTHOR_QUERY_PATTERN, searchString, startPos, maxResults, ascending?"ascending":"descending");
        LOGGER.debug("Builded query author link: \n" + queryLink);
        return queryLink;
    }
    
    
    public String buildForJournal()
    {
        String operator;
        operator = operatorToString(outerOperator);
        
        String titleKeysString = prepareString(titleKeys, titleKeysInnerOperator, "ti:");
        String abstractKeysString = prepareString(abstractKeys, abstractKeysInnerOperator, "abs:");
        LOGGER.debug("stworzony abstractKeysString: " + abstractKeysString);
        LOGGER.debug("stworzony titleKeysString: " + titleKeysString);
        
        String queryLink;
        if (titleKeysString == null || titleKeysString.isEmpty()) {
            queryLink = String.format(API_JOURNAL_SINGLE_QUERY_PATTERN, abstractKeysString, startPos, maxResults, ascending ? "ascending" : "descending");
        } else if (abstractKeysString == null || abstractKeysString.isEmpty()) {
            queryLink = String.format(API_JOURNAL_SINGLE_QUERY_PATTERN, titleKeysString, startPos, maxResults, ascending ? "ascending" : "descending");
        } else {
            queryLink = String.format(API_JOURNAL_QUERY_PATTERN, titleKeysString, operator, abstractKeysString, startPos, maxResults, ascending ? "ascending" : "descending");
        }
            
        LOGGER.debug("Builded query author link: \n" + queryLink);
        return queryLink;  
    }

    private String operatorToString(ComboCondition.Op operator) {
        if (operator == ComboCondition.Op.AND) {
            return "AND";
        } else {
            return "OR";
        }
    }

    private String prepareString(List<String> stringList, ComboCondition.Op operator, String filter) {
        if(stringList.isEmpty())
            return "";
        
        String op = operatorToString(operator);

        String result = "";
        for (int j = 0; j < stringList.size(); ++j) {
            String[] filtrList = stringList.get(j).split(" ");
            for (int i = 0; i < filtrList.length; ++i) {
                result += filter;
                result += filtrList[i];
                if (filtrList.length == i + 1) {
                    break;
                } else {
                    result += "+" + op + "+";
                }
            }
            if (j + 1 == stringList.size()) {
                break;
            } else {
                result += "+" + op + "+";
            }
        }

        return result;
    }
}
