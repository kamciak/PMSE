package com.publicationmetasearchengine.dao.publications;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InCondition;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.OrderObject;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.publicationmetasearchengine.dao.DBSchema;
import com.publicationmetasearchengine.dao.publications.exceptions.PublicationAlreadyExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.PublicationDoesNotExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.RelationAlreadyExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.RelationDoesNotExistException;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.data.filters.FilterCriteria;
import com.publicationmetasearchengine.data.filters.FilterType;
import com.publicationmetasearchengine.utils.DateUtils;
import com.publicationmetasearchengine.utils.MySQLUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public class PublicationDAOImpl implements PublicationDAO {
    private static final Logger LOGGER = Logger.getLogger(PublicationDAOImpl.class);

    private static final String SOURCE_TITLE_COLUMN_ALIAS = "sourceTitle";

    private class PublicationRowMapper implements RowMapper {

        private final boolean useAuthorName;
        private final boolean useSourceTitle;

        public PublicationRowMapper(boolean useAuthorName, boolean useSourceTitle) {
            this.useAuthorName = useAuthorName;
            this.useSourceTitle = useSourceTitle;
        }

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            Publication result = new Publication(
                    rs.getInt(DBSchema.PUBLICATION_ID_COLUMN.getName()),
                    null,
                    rs.getString(DBSchema.PUBLICATION_ARTICLE_ID_COLUMN.getName()),
                    useAuthorName?rs.getString(DBSchema.AUTHOR_NAME_COLUMN.getName()): null,
                    rs.getString(DBSchema.PUBLICATION_TITLE_COLUMN.getName()),
                    rs.getString(DBSchema.PUBLICATION_SUMMARY_COLUMN.getName()),
                    rs.getString(DBSchema.PUBLICATION_DOI_COLUMN.getName()),
                    useSourceTitle?rs.getString(SOURCE_TITLE_COLUMN_ALIAS):null,
                    rs.getString(DBSchema.PUBLICATION_SOURCE_VOLUME_COLUMN.getName()),
                    rs.getString(DBSchema.PUBLICATION_SOURCE_ISSUE_COLUMN.getName()),
                    rs.getString(DBSchema.PUBLICATION_SOURCE_PAGERANGE_COLUMN.getName()),
                    rs.getTimestamp(DBSchema.PUBLICATION_PUBLICATION_DATE_COLUMN.getName()),
                    rs.getString(DBSchema.PUBLICATION_PDF_LINK_COLUMN.getName()),
                    rs.getTimestamp(DBSchema.PUBLICATION_INSERT_DATE_COLUMN.getName())
            );
            result.setSourceDbId(rs.getInt(DBSchema.PUBLICATION_SOURCEDB_ID_COLUMN.getName()));
            return result;
        }
    }

    private class UserPublicationsRowMapper implements RowMapper {

        private final
                Map<Integer, Date> dateIdMap = new HashMap<Integer, Date>();
        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            dateIdMap.put(rs.getInt(DBSchema.USERPUBLICATIONS_PUBLICATION_ID_COLUMN.getName()),
                    rs.getTimestamp(DBSchema.USERPUBLICATIONS_INSERT_DATE_COLUMN.getName()));
            return new Object();
        }

        public Map<Integer, Date> getDateIdMap() {
            return dateIdMap;
        }
    }

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Integer insertPublication(int sourceDBId, String articleId, int mainAuthorId,
            String title, String summary, String doi, Integer sourceTitleId,
            String sourceVolume, String sourceIssue, String sourcePageRange,
            Date publicationDate, String pdfLink) throws PublicationAlreadyExistException {
        final Date insertDate = new Date();
        final String insertQuery = new InsertQuery(DBSchema.PUBLICATION_TABLE)
                .addColumn(DBSchema.PUBLICATION_SOURCEDB_ID_COLUMN, sourceDBId)
                .addColumn(DBSchema.PUBLICATION_ARTICLE_ID_COLUMN, articleId)
                .addColumn(DBSchema.PUBLICATION_MAIN_AUTHOR_ID_COLUMN, mainAuthorId)
                .addColumn(DBSchema.PUBLICATION_TITLE_COLUMN, MySQLUtils.escapeSpecialCharacters(title))
                .addColumn(DBSchema.PUBLICATION_SUMMARY_COLUMN, MySQLUtils.escapeSpecialCharacters(summary))
                .addColumn(DBSchema.PUBLICATION_DOI_COLUMN, doi!=null?MySQLUtils.escapeSpecialCharacters(doi):null)
                .addColumn(DBSchema.PUBLICATION_SOURCE_TITLE_ID_COLUMN, sourceTitleId)
                .addColumn(DBSchema.PUBLICATION_SOURCE_VOLUME_COLUMN, sourceVolume)
                .addColumn(DBSchema.PUBLICATION_SOURCE_ISSUE_COLUMN, sourceIssue)
                .addColumn(DBSchema.PUBLICATION_SOURCE_PAGERANGE_COLUMN, sourcePageRange)
                .addColumn(DBSchema.PUBLICATION_PUBLICATION_DATE_COLUMN, DateUtils.formatDate(publicationDate))
                .addColumn(DBSchema.PUBLICATION_PDF_LINK_COLUMN, pdfLink!=null?MySQLUtils.escapeSpecialCharacters(pdfLink):null)
                .addColumn(DBSchema.PUBLICATION_INSERT_DATE_COLUMN, DateUtils.formatDate(insertDate))
                .toString();
        LOGGER.debug("Query to execute:" + insertQuery);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try{
            jdbcTemplate.update(new PreparedStatementCreator() {

                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    return con.prepareStatement(insertQuery, new String[]{DBSchema.PUBLICATION_ID_COLUMN.getName()});
                }
            }, keyHolder);

            return keyHolder.getKey().intValue();
        } catch (DataIntegrityViolationException ex) {
            throw new PublicationAlreadyExistException(String.format("[%d-%s]", sourceDBId, articleId));
        }
    }

    @Override
    public Publication getPublication(int sourceDbId, String articleId) throws PublicationDoesNotExistException {
        String selectQuery = new SelectQuery()
                .addFromTable(DBSchema.PUBLICATION_TABLE)
                .addAllColumns()
                .addCondition(new ComboCondition(ComboCondition.Op.AND,
                    new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.PUBLICATION_SOURCEDB_ID_COLUMN, sourceDbId),
                    new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.PUBLICATION_ARTICLE_ID_COLUMN, articleId)
                ))
                .toString();
        try {
            return (Publication) jdbcTemplate.queryForObject(selectQuery, new PublicationRowMapper(false, false));
        } catch (EmptyResultDataAccessException ex) {
            throw new PublicationDoesNotExistException(String.format("[%d-%s]", sourceDbId, articleId));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Publication> getPublicationsBySourceDbIdDate(int sourceDbId, Date date) {
        ComboCondition conditions = new ComboCondition(ComboCondition.Op.AND,
                new BinaryCondition(BinaryCondition.Op.GREATER_THAN_OR_EQUAL_TO, DBSchema.PUBLICATION_PUBLICATION_DATE_COLUMN, DateUtils.formatDateOnly(date))
        );
        if (sourceDbId >=0 )
            conditions.addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.PUBLICATION_SOURCEDB_ID_COLUMN, sourceDbId));
        String selectQuery = new SelectQuery()
                .addAllTableColumns(DBSchema.PUBLICATION_TABLE)
                .addColumns(DBSchema.AUTHOR_NAME_COLUMN)
                .addAliasedColumn(DBSchema.SOURCETITLE_TITLE_COLUMN, SOURCE_TITLE_COLUMN_ALIAS)
                .addFromTable(DBSchema.PUBLICATION_TABLE)
                .addJoins(SelectQuery.JoinType.INNER, DBSchema.PUBLICATION_MAINAUTHOR_JOIN)
                .addJoins(SelectQuery.JoinType.LEFT_OUTER, DBSchema.PUBLICATION_SOURCETITLE_JOIN)
                .addCondition(conditions)
                .addOrdering(DBSchema.PUBLICATION_PUBLICATION_DATE_COLUMN, OrderObject.Dir.DESCENDING).toString();
        return (ArrayList<Publication>) jdbcTemplate.query(selectQuery, new PublicationRowMapper(true, true));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Publication> getPublicationsByIds(List<Integer> ids) {
        String selectQuery = new SelectQuery()
                .addAllTableColumns(DBSchema.PUBLICATION_TABLE)
                .addColumns(DBSchema.AUTHOR_NAME_COLUMN)
                .addAliasedColumn(DBSchema.SOURCETITLE_TITLE_COLUMN, SOURCE_TITLE_COLUMN_ALIAS)
                .addColumns(DBSchema.AUTHOR_NAME_COLUMN)
                .addFromTable(DBSchema.PUBLICATION_TABLE)
                .addJoins(SelectQuery.JoinType.INNER, DBSchema.PUBLICATION_MAINAUTHOR_JOIN)
                .addJoins(SelectQuery.JoinType.LEFT_OUTER, DBSchema.PUBLICATION_SOURCETITLE_JOIN)
                .addCondition(new InCondition(DBSchema.PUBLICATION_ID_COLUMN, ids))
                .toString();
        return (ArrayList<Publication>) jdbcTemplate.query(selectQuery, new PublicationRowMapper(true, true));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Publication> getPublicationOfAuthor(String mainAuthorId){
        String selectQuery = new SelectQuery()
                .addAllTableColumns(DBSchema.PUBLICATION_TABLE)
                .addColumns(DBSchema.AUTHOR_NAME_COLUMN)
                .addAliasedColumn(DBSchema.SOURCETITLE_TITLE_COLUMN, SOURCE_TITLE_COLUMN_ALIAS)
                .addFromTable(DBSchema.PUBLICATION_TABLE)
                .addJoins(SelectQuery.JoinType.INNER, DBSchema.PUBLICATION_MAINAUTHOR_JOIN)
                .addJoins(SelectQuery.JoinType.LEFT_OUTER, DBSchema.PUBLICATION_SOURCETITLE_JOIN)
                .addCondition(new ComboCondition(ComboCondition.Op.AND,
                    new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.AUTHOR_NAME_COLUMN, mainAuthorId)
                ))
                .toString();
        return (ArrayList<Publication>) jdbcTemplate.query(selectQuery.toString(), new PublicationRowMapper(true, true));
    }

    @Override
    @SuppressWarnings({"unchecked", "unchecked"})
    public List<Publication> getPublicationsByFiltersCritaria(List<FilterCriteria> filtersCritaria, Date afterDate) {
        SelectQuery selectQuery = new SelectQuery()
                .addAllTableColumns(DBSchema.PUBLICATION_TABLE)
                .addColumns(DBSchema.AUTHOR_NAME_COLUMN)
                .addAliasedColumn(DBSchema.SOURCETITLE_TITLE_COLUMN, SOURCE_TITLE_COLUMN_ALIAS)
                .addFromTable(DBSchema.PUBLICATION_TABLE)
                .addJoins(SelectQuery.JoinType.INNER, DBSchema.PUBLICATION_MAINAUTHOR_JOIN)
                .addJoins(SelectQuery.JoinType.LEFT_OUTER, DBSchema.PUBLICATION_SOURCETITLE_JOIN);
        ComboCondition mainComboCondition = new ComboCondition(ComboCondition.Op.AND);
        for (FilterCriteria filter : filtersCritaria) {
            if (filter.getValues() == null)
                continue;
            if (filter.getFilterType() == FilterType.PUBLICATION_DATE) {
                ComboCondition dateCondition = new ComboCondition(ComboCondition.Op.AND);
                if (filter.getValues().get(0)!=null)
                    dateCondition.addConditions(new BinaryCondition(BinaryCondition.Op.GREATER_THAN_OR_EQUAL_TO, filter.getFilterType().getDbColumn(), filter.getValues().get(0)));
                if (filter.getValues().get(1)!=null)
                    dateCondition.addConditions(new BinaryCondition(BinaryCondition.Op.LESS_THAN_OR_EQUAL_TO, filter.getFilterType().getDbColumn(), filter.getValues().get(1)));
                mainComboCondition.addCondition(dateCondition);
                continue;
            }
            if (filter.getFilterType() == FilterType.AUTHOR) {
                if (filter.getValues().isEmpty())
                    continue;
                for(String author : filter.getValues())
                    mainComboCondition.addCondition(new InCondition(DBSchema.PUBLICATION_ID_COLUMN, generateAuthorFilterSubQuery(filter, author)));
                continue;
            }

            ComboCondition filterComboCondition = new ComboCondition(filter.getOuterOperator());
            for(String andString : filter.getValues()) {
                String [] parts = andString.split(",");
                ComboCondition partsComboCondition = new ComboCondition(filter.getInnerOperator());
                for (String part : parts)
                    if (part.startsWith("!"))
                        partsComboCondition.addCondition(BinaryCondition.notLike(filter.getFilterType().getDbColumn(), "%"+part.substring(1).trim()+"%"));
                    else
                        partsComboCondition.addCondition(BinaryCondition.like(filter.getFilterType().getDbColumn(), "%"+part.trim()+"%"));
                filterComboCondition.addCondition(partsComboCondition);
            }

            mainComboCondition.addCondition(filterComboCondition);
        }
        if (afterDate != null)
            mainComboCondition.addCondition(new BinaryCondition(BinaryCondition.Op.GREATER_THAN_OR_EQUAL_TO, DBSchema.PUBLICATION_INSERT_DATE_COLUMN, DateUtils.formatDate(afterDate)));
        selectQuery.addCondition(mainComboCondition);
        selectQuery.addOrdering(DBSchema.PUBLICATION_PUBLICATION_DATE_COLUMN, OrderObject.Dir.DESCENDING);

        return (ArrayList<Publication>) jdbcTemplate.query(selectQuery.toString(), new PublicationRowMapper(true, true));
    }

    private SelectQuery generateAuthorFilterSubQuery(FilterCriteria authorFilterCriteria, String author) {
        SelectQuery authorSubQuery = new SelectQuery(true)
                .addColumns(DBSchema.PUBLICATIONAUTHORS_PUBLICATION_ID_COLUMN)
                .addFromTable(DBSchema.PUBLICATIONAUTHORS_TABLE)
                .addJoins(SelectQuery.JoinType.INNER, DBSchema.PUBLICATIONAUTHORS_AUTHOR_JOIN);
        ComboCondition authorSubqueryCondition = new ComboCondition(authorFilterCriteria.getInnerOperator());
        String[] parts = author.split(" ");
        ComboCondition authorCondition = new ComboCondition(ComboCondition.Op.AND);
        authorCondition.addCondition(BinaryCondition.like(DBSchema.AUTHOR_NAME_COLUMN, String.format("%%%s %%", parts[0].trim())));//%surname %
        ComboCondition namesCondition = new ComboCondition(ComboCondition.Op.AND);
        for (int i = 1; i < parts.length; ++i){
            String name = parts[i];
            ComboCondition nameCondition = new ComboCondition(ComboCondition.Op.OR);
            if (name.endsWith("."))
                name = name.split("\\.")[0];
            nameCondition.addCondition(BinaryCondition.like(DBSchema.AUTHOR_NAME_COLUMN, String.format("%% %s%%",name))); //% name%
            nameCondition.addCondition(BinaryCondition.like(DBSchema.AUTHOR_NAME_COLUMN, String.format("%% %s.%%",name.charAt(0))));//% n.%
            namesCondition.addCondition(nameCondition);
        }
        authorCondition.addCondition(namesCondition);
        authorSubqueryCondition.addCondition(authorCondition);
        authorSubQuery.addCondition(authorSubqueryCondition);
        return authorSubQuery;
    }

    @Override
    public void insertUserPublication(int userId, int publicationId) throws RelationAlreadyExistException {
        final Date insertDate = new Date();
        String insertQuery = new InsertQuery(DBSchema.USERPUBLICATIONS_TABLE)
                .addColumn(DBSchema.USERPUBLICATIONS_USER_ID_COLUMN, userId)
                .addColumn(DBSchema.USERPUBLICATIONS_PUBLICATION_ID_COLUMN, publicationId)
                .addColumn(DBSchema.USERPUBLICATIONS_INSERT_DATE_COLUMN, DateUtils.formatDate(insertDate))
                .toString();
        try{
            jdbcTemplate.execute(insertQuery);
        } catch (DataIntegrityViolationException ex) {
            throw new RelationAlreadyExistException("" + userId + " - " + publicationId);
        }
    }

    @Override
    public boolean checkUserPublication(int userId, int publicationId) {
        String selectQuery = new SelectQuery()
                .addFromTable(DBSchema.USERPUBLICATIONS_TABLE)
                .addColumns(DBSchema.USERPUBLICATIONS_PUBLICATION_ID_COLUMN)
                .addCondition(
                    new ComboCondition(ComboCondition.Op.AND,
                        new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.USERPUBLICATIONS_USER_ID_COLUMN, userId),
                        new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.USERPUBLICATIONS_PUBLICATION_ID_COLUMN, publicationId)
                    )
                ).toString();
        try {
            jdbcTemplate.queryForInt(selectQuery);
            return true;
        }catch (DataAccessException ex) {
            return false;
        }
    }

    @Override
    public void removeUserPublication(int userId, int publicationId) throws RelationDoesNotExistException {
        String deleteQuery = new DeleteQuery(DBSchema.USERPUBLICATIONS_TABLE)
                .addCondition(
                    new ComboCondition(ComboCondition.Op.AND,
                        new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.USERPUBLICATIONS_USER_ID_COLUMN, userId),
                        new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.USERPUBLICATIONS_PUBLICATION_ID_COLUMN, publicationId)
                    )
                ).toString();
        if (jdbcTemplate.update(deleteQuery) ==0 )
            throw new RelationDoesNotExistException("" + userId + " - " + publicationId);
    }

    @Override
    public void removeUserPublications(int userId) throws RelationDoesNotExistException {
        String deleteQuery = new DeleteQuery(DBSchema.USERPUBLICATIONS_TABLE)
                .addCondition(
                    new ComboCondition(ComboCondition.Op.AND,
                        new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.USERPUBLICATIONS_USER_ID_COLUMN, userId)
                    )
                ).toString();
        if (jdbcTemplate.update(deleteQuery) ==0 )
            throw new RelationDoesNotExistException(""+userId);
    }

    @Override
    public Map<Integer, Date> getUserPublicationsIds(int userId) {
        String selectQuery = new SelectQuery()
                .addFromTable(DBSchema.USERPUBLICATIONS_TABLE)
                .addColumns(DBSchema.USERPUBLICATIONS_PUBLICATION_ID_COLUMN, DBSchema.USERPUBLICATIONS_INSERT_DATE_COLUMN)
                .addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.USERPUBLICATIONS_USER_ID_COLUMN, userId))
                .toString();
        UserPublicationsRowMapper userPublicationsRowMapper = new UserPublicationsRowMapper();
        jdbcTemplate.query(selectQuery, userPublicationsRowMapper);
        return userPublicationsRowMapper.getDateIdMap();
    }

    @Override
    public Date getNewestPublicationDateBySourceDBId(int sourceDbId) {
        String selectQuery = new SelectQuery()
                .addFromTable(DBSchema.PUBLICATION_TABLE)
                .addCustomColumns(DBSchema.getFunctionCall(DBSchema.FUNCTION_MAX, DBSchema.PUBLICATION_PUBLICATION_DATE_COLUMN))
                .addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.PUBLICATION_SOURCEDB_ID_COLUMN, sourceDbId))
                .toString();
        try {
            return (Date)jdbcTemplate.queryForObject(selectQuery, Date.class);
        }catch(DataAccessException ex) {
            return null;
        }
    }
    
    @Override
    public List<Publication> getAllPublications(){
        String selectQuery = new SelectQuery()
                .addAllTableColumns(DBSchema.PUBLICATION_TABLE)
                .addColumns(DBSchema.AUTHOR_NAME_COLUMN)
                .addAliasedColumn(DBSchema.SOURCETITLE_TITLE_COLUMN, SOURCE_TITLE_COLUMN_ALIAS)
                .addFromTable(DBSchema.PUBLICATION_TABLE)
                .addJoins(SelectQuery.JoinType.INNER, DBSchema.PUBLICATION_MAINAUTHOR_JOIN)
                .addJoins(SelectQuery.JoinType.LEFT_OUTER, DBSchema.PUBLICATION_SOURCETITLE_JOIN).toString();
        
        return (ArrayList<Publication>) jdbcTemplate.query(selectQuery, new PublicationRowMapper(true, true));
    }
}
