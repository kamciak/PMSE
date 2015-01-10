package com.publicationmetasearchengine.dao.filtercriterias;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;
import com.publicationmetasearchengine.dao.DBSchema;
import com.publicationmetasearchengine.dao.filtercriterias.exceptions.FilterCriteriasDoesNotExistException;
import com.publicationmetasearchengine.data.filters.NamedFilterCriteria;
import com.publicationmetasearchengine.utils.DateUtils;
import com.publicationmetasearchengine.utils.XMLUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public class FilterCriteriasDAOImpl implements FilterCriteriaDAO {
    private static final Logger LOGGER = Logger.getLogger(FilterCriteriasDAOImpl.class);

    private class FilterCriteriaRowMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            NamedFilterCriteria nfc = XMLUtils.deserializeFilterCritaria(rs.getString(DBSchema.FILTERCRITERIAS_FILTERS_COLUMN.getName()));
            nfc.setId(rs.getInt(DBSchema.FILTERCRITERIAS_ID_COLUMN.getName()));
            nfc.setLastSearchDate(rs.getTimestamp(DBSchema.FILTERCRITERIAS_LAST_SEARCH_DATE_COLUMN.getName()));
            nfc.setOwnerId(rs.getInt(DBSchema.FILTERCRITERIAS_USER_ID_COLUMN.getName()));
            return nfc;
        }

    }


    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public int addFilterCriteriasForUser(NamedFilterCriteria filterCriteria, int userId) {
        final String insertQuery = new InsertQuery(DBSchema.FILTERCRITERIAS_TABLE)
                .addColumn(DBSchema.FILTERCRITERIAS_USER_ID_COLUMN, userId)
                .addColumn(DBSchema.FILTERCRITERIAS_FILTERS_COLUMN, XMLUtils.serializeFilterCritaria(filterCriteria))
                .addColumn(DBSchema.FILTERCRITERIAS_LAST_SEARCH_DATE_COLUMN, DateUtils.formatDate(new Date()))
                .toString();
        LOGGER.debug("Query to execute:" + insertQuery);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                return con.prepareStatement(insertQuery, new String[] {DBSchema.FILTERCRITERIAS_ID_COLUMN.getName()});
            }
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public void modifyFilterCriteriasById(NamedFilterCriteria newFilterCriteria, int filterCriteriaId) throws FilterCriteriasDoesNotExistException {
        String updateQuery = new UpdateQuery(DBSchema.FILTERCRITERIAS_TABLE)
                .addSetClause(DBSchema.FILTERCRITERIAS_FILTERS_COLUMN, XMLUtils.serializeFilterCritaria(newFilterCriteria))
                .addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.FILTERCRITERIAS_ID_COLUMN, filterCriteriaId))
                .toString();
        if (jdbcTemplate.update(updateQuery) == 0 ) {
            throw new FilterCriteriasDoesNotExistException(String.format("FilterCrierias [%d] not found", filterCriteriaId));
        }
    }

    @Override
    public void deleteFilterCriteriasByID(int filterCriteriaId) throws FilterCriteriasDoesNotExistException {
        String deleteQuery = new DeleteQuery(DBSchema.FILTERCRITERIAS_TABLE)
                .addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.FILTERCRITERIAS_ID_COLUMN, filterCriteriaId))
                .toString();
        if (jdbcTemplate.update(deleteQuery) == 0 ) {
            throw new FilterCriteriasDoesNotExistException(String.format("FilterCrierias [%d] not found", filterCriteriaId));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<NamedFilterCriteria> getAllFiltersCriterias() {
        String selectQuery = new SelectQuery()
                .addAllColumns()
                .addFromTable(DBSchema.FILTERCRITERIAS_TABLE)
                .toString();
        return jdbcTemplate.query(selectQuery, new FilterCriteriaRowMapper());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<NamedFilterCriteria> getAllFiltersCriteriasByUserId(int userId) {
        String selectQuery = new SelectQuery()
                .addAllColumns()
                .addFromTable(DBSchema.FILTERCRITERIAS_TABLE)
                .addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.FILTERCRITERIAS_USER_ID_COLUMN, userId))
                .toString();
        return jdbcTemplate.query(selectQuery, new FilterCriteriaRowMapper());
    }

    @Override
    public void touchFilterCriteriasById(int filterCriteriaId) throws FilterCriteriasDoesNotExistException {
        String updateQuery = new UpdateQuery(DBSchema.FILTERCRITERIAS_TABLE)
                .addSetClause(DBSchema.FILTERCRITERIAS_LAST_SEARCH_DATE_COLUMN, DateUtils.formatDate(new Date()))
                .addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.FILTERCRITERIAS_ID_COLUMN, filterCriteriaId))
                .toString();
        if (jdbcTemplate.update(updateQuery) == 0 ) {
            throw new FilterCriteriasDoesNotExistException(String.format("FilterCrierias [%d] not found", filterCriteriaId));
        }
    }

}
