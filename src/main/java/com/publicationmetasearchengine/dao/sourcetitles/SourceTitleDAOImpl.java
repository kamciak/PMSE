package com.publicationmetasearchengine.dao.sourcetitles;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.publicationmetasearchengine.dao.DBSchema;
import com.publicationmetasearchengine.dao.sourcetitles.exceptions.SourceTitleAlreadyExists;
import com.publicationmetasearchengine.dao.sourcetitles.exceptions.SourceTitleDoesNotExists;
import com.publicationmetasearchengine.utils.MySQLUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public class SourceTitleDAOImpl implements SourceTitleDAO {
    private static final Logger LOGGER = Logger.getLogger(SourceTitleDAOImpl.class);

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Integer addTitle(String title) throws SourceTitleAlreadyExists {
        final String insertQuery = new InsertQuery(DBSchema.SOURCETITLE_TABLE)
                .addColumn(DBSchema.SOURCETITLE_TITLE_COLUMN, MySQLUtils.escapeSpecialCharacters(title))
                .toString();
        LOGGER.debug("Query to execute:" + insertQuery);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(new PreparedStatementCreator() {

                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    return con.prepareStatement(insertQuery, new String[]{DBSchema.SOURCETITLE_ID_COLUMN.getName()});
                }
            }, keyHolder);
            LOGGER.info(String.format("Source [%s] added under ID: %d", title, keyHolder.getKey().intValue()));
            return keyHolder.getKey().intValue();
        } catch (DataAccessException ex) {
            if (DataIntegrityViolationException.class.equals(ex.getClass())) {
                throw new SourceTitleAlreadyExists(String.format("Title [%s] already exists", title));
            }
        }
        return null;
    }

    @Override
    public Integer getTitleIdByTitle(String title) throws SourceTitleDoesNotExists {
        String selectQuery = new SelectQuery()
                .addColumns(DBSchema.SOURCETITLE_ID_COLUMN)
                .addFromTable(DBSchema.SOURCETITLE_TABLE)
                .addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.SOURCETITLE_TITLE_COLUMN, title))
                .toString();
        try {
            return jdbcTemplate.queryForInt(selectQuery);
        } catch (EmptyResultDataAccessException ex) {
            throw new SourceTitleDoesNotExists(title);
        }
    }
}
