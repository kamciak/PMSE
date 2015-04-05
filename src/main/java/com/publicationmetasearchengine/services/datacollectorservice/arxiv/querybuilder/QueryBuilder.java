package com.publicationmetasearchengine.services.datacollectorservice.arxiv.querybuilder;

import org.apache.log4j.Logger;

public class QueryBuilder {
    private static final Logger LOGGER = Logger.getLogger(QueryBuilder.class);

    private static final String API_QUERY_PATTERN = "http://export.arxiv.org/api/query?search_query=cat:%s&start=%d&max_results=%d&sortBy=submittedDate&sortOrder=%s";
    private static final String API_AUTHOR_QUERY_PATTERN = "http://export.arxiv.org/api/query?search_query=au:%s&start=%d&max_results=%d&sortBy=submittedDate&sortOrder=%s";

    private final String searchString;
    private final int startPos;
    private final int maxResults;
    private final boolean ascending;

    public QueryBuilder(String searchString, int startPos, int maxResults, boolean ascending) {
        this.searchString = searchString;
        this.startPos = startPos;
        this.maxResults = maxResults;
        this.ascending = ascending;
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
}
