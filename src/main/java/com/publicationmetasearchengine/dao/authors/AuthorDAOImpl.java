package com.publicationmetasearchengine.dao.authors;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.publicationmetasearchengine.dao.DBSchema;
import com.publicationmetasearchengine.dao.authors.exceptions.AuthorAlreadyExistException;
import com.publicationmetasearchengine.dao.authors.exceptions.AuthorDoesNotExistException;
import com.publicationmetasearchengine.dao.publications.exceptions.PublicationWithNoAuthorException;
import com.publicationmetasearchengine.data.Author;
import com.publicationmetasearchengine.utils.MySQLUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

public class AuthorDAOImpl implements AuthorDAO {
    private static final Logger LOGGER = Logger.getLogger(AuthorDAOImpl.class);

    private class AuthorRowMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Author(
                    rs.getInt(DBSchema.AUTHOR_ID_COLUMN.getName()),
                    rs.getString(DBSchema.AUTHOR_NAME_COLUMN.getName()));
        }
    }

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Integer insertAuthor(String name) throws AuthorAlreadyExistException {
        final String insertQuery = new InsertQuery(DBSchema.AUTHOR_TABLE)
                .addColumn(DBSchema.AUTHOR_NAME_COLUMN, MySQLUtils.escapeSpecialCharacters(name))
                .toString();
        LOGGER.debug("Query to execute:" + insertQuery);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(new PreparedStatementCreator() {

                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    return con.prepareStatement(insertQuery, new String[] {DBSchema.AUTHOR_ID_COLUMN.getName()});
                }
            }, keyHolder);

            return keyHolder.getKey().intValue();
        } catch (DataAccessException ex) {
            if (DataIntegrityViolationException.class.equals(ex.getClass())) {
                throw new AuthorAlreadyExistException(String.format("Author [%s] already exists", name));
            }
        }
        return null;
    }

    @Override
    public Author getAuthorById(int authorId) throws AuthorDoesNotExistException {
        String selectQuery = new SelectQuery()
                .addFromTable(DBSchema.AUTHOR_TABLE)
                .addAllColumns()
                .addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.AUTHOR_ID_COLUMN, authorId))
                .toString();
        try {
            return (Author) jdbcTemplate.queryForObject(selectQuery, new AuthorRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            throw new AuthorDoesNotExistException(String.format("Author with ID [%d] not found", authorId));
        }
    }

    @Override
    public Integer getAuthorId(String name) throws AuthorDoesNotExistException {
        String selectQuery = new SelectQuery()
                .addColumns(DBSchema.AUTHOR_ID_COLUMN)
                .addFromTable(DBSchema.AUTHOR_TABLE)
                .addCondition(
                    new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.AUTHOR_NAME_COLUMN, MySQLUtils.escapeSpecialCharacters(name)))
                .toString();
        try {
            return jdbcTemplate.queryForInt(selectQuery);
        } catch (EmptyResultDataAccessException ex) {
            throw new AuthorDoesNotExistException(String.format("Author [%s] not found", name));
        }
    }

    @Override
    public ArrayList<Author> getPublicationAuthorsById(int publicationId) throws PublicationWithNoAuthorException {
        String selectQuery = new SelectQuery()
                .addFromTable(DBSchema.AUTHOR_TABLE)
                .addAllColumns()
                .addJoins(SelectQuery.JoinType.INNER, DBSchema.AUTHOR_PUBLICATIONAUTHORS_JOIN)
                .addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.PUBLICATIONAUTHORS_PUBLICATION_ID_COLUMN, publicationId))
                .toString();
        @SuppressWarnings("unchecked")
        ArrayList<Author> authors = (ArrayList<Author>) jdbcTemplate.query(selectQuery, new AuthorRowMapper());
        if (authors.isEmpty()) {
            throw new PublicationWithNoAuthorException(String.format("Publication with ID [%d] has no authors", publicationId));
        }
        return authors;
    }

    @Override
    public void setPublicationAuthorsById(int publicationId, List<Integer> authorsIds) {
        Set<Integer> authorUniqueIds = new HashSet<Integer>(authorsIds);
        for (Integer authorId : authorUniqueIds) {
            String insertQuery = new InsertQuery(DBSchema.PUBLICATIONAUTHORS_TABLE)
                    .addColumn(DBSchema.PUBLICATIONAUTHORS_PUBLICATION_ID_COLUMN, publicationId)
                    .addColumn(DBSchema.PUBLICATIONAUTHORS_AUTHOR_ID_COLUMN, authorId)
                    .toString();
            jdbcTemplate.update(insertQuery);
        }
    }

    @Override
    public void clearPublicationAuthors(int publicationId) throws PublicationWithNoAuthorException {
        String deleteQuery = new DeleteQuery(DBSchema.PUBLICATIONAUTHORS_TABLE)
                .addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.PUBLICATIONAUTHORS_PUBLICATION_ID_COLUMN, publicationId))
                .toString();
        if (jdbcTemplate.update(deleteQuery) == 0)
            throw new PublicationWithNoAuthorException("Id: " + publicationId);
    }


}
