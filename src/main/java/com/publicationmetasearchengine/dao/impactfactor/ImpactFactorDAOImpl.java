/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.dao.impactfactor;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.publicationmetasearchengine.dao.DBSchema;
import com.publicationmetasearchengine.dao.impactfactor.exceptions.JournalAlreadyExistException;
import com.publicationmetasearchengine.dao.impactfactor.exceptions.JournalDoesNotExistException;
import com.publicationmetasearchengine.data.Journal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

/**
 *
 * @author Kamciak
 */
public class ImpactFactorDAOImpl implements ImpactFactorDAO {

    private static final Logger LOGGER = Logger.getLogger(ImpactFactorDAOImpl.class);

    private class JournalRowMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Journal(
                    rs.getInt(DBSchema.IMPACTFACTOR_ID_COLUMN.getName()),
                    rs.getString(DBSchema.IMPACTFACTOR_NAME_COLUMN.getName()),
                    rs.getString(DBSchema.IMPACTFACTOR_ISSN_COLUMN.getName()),
                    rs.getFloat(DBSchema.IMPACTFACTOR_2013_2014_COLUMN.getName()),
                    rs.getFloat(DBSchema.IMPACTFACTOR_2012_COLUMN.getName()),
                    rs.getFloat(DBSchema.IMPACTFACTOR_2011_COLUMN.getName()),
                    rs.getFloat(DBSchema.IMPACTFACTOR_2010_COLUMN.getName()),
                    rs.getFloat(DBSchema.IMPACTFACTOR_2009_COLUMN.getName()),
                    rs.getFloat(DBSchema.IMPACTFACTOR_2008_COLUMN.getName()));
        }
    }
    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Integer insertImpactFactor(Journal journal) throws JournalAlreadyExistException {
        DbColumn[] columns = {
            DBSchema.IMPACTFACTOR_NAME_COLUMN,
            DBSchema.IMPACTFACTOR_ISSN_COLUMN,
            DBSchema.IMPACTFACTOR_2013_2014_COLUMN,
            DBSchema.IMPACTFACTOR_2012_COLUMN,
            DBSchema.IMPACTFACTOR_2011_COLUMN,
            DBSchema.IMPACTFACTOR_2010_COLUMN,
            DBSchema.IMPACTFACTOR_2009_COLUMN,
            DBSchema.IMPACTFACTOR_2008_COLUMN
        };
        Object[] values = {
            journal.getTitle(),
            journal.getISSN(),
            journal.getImpactFactor2013_2014(),
            journal.getImpactFactor2012(),
            journal.getImpactFactor2011(),
            journal.getImpactFactor2010(),
            journal.getImpactFactor2009(),
            journal.getImpactFactor2008()
        };

        final String insertQuery = new InsertQuery(DBSchema.IMPACTFACTOR_TABLE)
                .addColumns(columns, values)
                .toString();

        //LOGGER.debug("Query to execute:" + insertQuery);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    return con.prepareStatement(insertQuery, new String[]{DBSchema.IMPACTFACTOR_ID_COLUMN.getName()});
                }
            }, keyHolder);

            return keyHolder.getKey().intValue();
        } catch (DataAccessException ex) {
            if (DataIntegrityViolationException.class.equals(ex.getClass())) {
                throw new JournalAlreadyExistException(String.format("Journal [%s] already exists", journal.toString()));
            }
        }
        return null;
    }

    @Override
    public Journal getJournalById(int journalId) throws JournalDoesNotExistException {
        String selectQuery = new SelectQuery()
                .addFromTable(DBSchema.IMPACTFACTOR_TABLE)
                .addAllColumns()
                .addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.IMPACTFACTOR_ID_COLUMN, journalId))
                .toString();

        try {
            return (Journal) jdbcTemplate.queryForObject(selectQuery, new JournalRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            throw new JournalDoesNotExistException(String.format("Journal with ID [%d] not found", journalId));
        }
    }

    @Override
    public Journal getJournalByISSN(String ISSN) throws JournalDoesNotExistException {
        String selectQuery = new SelectQuery()
                .addFromTable(DBSchema.IMPACTFACTOR_TABLE)
                .addAllColumns()
                .addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.IMPACTFACTOR_ISSN_COLUMN, ISSN))
                .toString();

        try {
            return (Journal) jdbcTemplate.queryForObject(selectQuery, new JournalRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            throw new JournalDoesNotExistException(String.format("Journal with ISSN [%d] not found", ISSN));
        }
    }

    @Override
    public List<Journal> getAllJournals() {
        String selectQuery = new SelectQuery()
                .addFromTable(DBSchema.IMPACTFACTOR_TABLE)
                .addAllColumns()
                .toString();

        return (ArrayList<Journal>) jdbcTemplate.query(selectQuery, new JournalRowMapper());

    }
    @Override
    public List<Journal> getJournalsStartAt(char firstChar){
        String selecQuery = new SelectQuery()
                .addFromTable(DBSchema.IMPACTFACTOR_TABLE)
                .addAllColumns()
                .addCondition(BinaryCondition.like(DBSchema.IMPACTFACTOR_NAME_COLUMN, firstChar+"%"))
                .toString();
        
        return (ArrayList<Journal>) jdbcTemplate.query(selecQuery, new JournalRowMapper());
                
    }
}
