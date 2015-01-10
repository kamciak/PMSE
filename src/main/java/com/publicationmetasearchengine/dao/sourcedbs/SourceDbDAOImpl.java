package com.publicationmetasearchengine.dao.sourcedbs;

import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.publicationmetasearchengine.dao.DBSchema;
import com.publicationmetasearchengine.dao.sourcedbs.exceptions.SourceDbDoesNotExistException;
import com.publicationmetasearchengine.data.SourceDB;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class SourceDbDAOImpl implements SourceDbDAO {
    private static final Logger LOGGER = Logger.getLogger(SourceDbDAOImpl.class);

    private Map<Integer, SourceDB> idToSourceDb;

    private class SourceDbRowMapper implements RowMapper {

        private final Map<Integer, SourceDB> idToSourceDb;

        public SourceDbRowMapper(Map<Integer, SourceDB> idToSourceDb) {
            this.idToSourceDb = idToSourceDb;
        }

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            int id = rs.getInt(DBSchema.SOURCEDB_ID_COLUMN.getName());
            idToSourceDb.put(id, new SourceDB(
                    id,
                    rs.getString(DBSchema.SOURCEDB_FULLNAME_COLUMN.getName()),
                    rs.getString(DBSchema.SOURCEDB_SHORTNAME_COLUMN.getName())
                ));
            return new Object();
        }
    }

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public int getSourceIdByShortName(String shortName) throws SourceDbDoesNotExistException {
        if (idToSourceDb == null)
            populateMaps();
        Integer id = null;
        for (Map.Entry<Integer, SourceDB> entry : idToSourceDb.entrySet()) {
            id = entry.getKey();
            SourceDB sourceDB = entry.getValue();
            if (sourceDB.getShortName().equals(shortName))
                return id;
        }
        throw new SourceDbDoesNotExistException(String.format("Source [%s] does not exists", shortName));
    }

    @Override
    public SourceDB getSourceDBById(int sourceId) throws SourceDbDoesNotExistException {
        if (idToSourceDb == null)
            populateMaps();
        SourceDB sourceDB = idToSourceDb.get(sourceId);
        if (sourceDB == null) {
            throw new SourceDbDoesNotExistException(String.format("Source with id [%d] does not exists", sourceId));
        }
        return sourceDB;
    }

    @Override
    public List<SourceDB> getAllSourceDBS() {
        if (idToSourceDb == null)
            populateMaps();
        List<SourceDB> result = new ArrayList<SourceDB>();
        result.add(ALL_DB_SOURCE);
        for (Map.Entry<Integer, SourceDB> entry : idToSourceDb.entrySet())
            result.add(entry.getValue());
        return result;
    }

    @SuppressWarnings("unchecked")
    private void populateMaps() {
        String selectQuery = new SelectQuery()
                .addFromTable(DBSchema.SOURCEDB_TABLE)
                .addAllColumns()
                .toString();
        idToSourceDb = new HashMap<Integer, SourceDB>();
        List<SourceDB> sourceList = jdbcTemplate.query(selectQuery, new SourceDbRowMapper(idToSourceDb));
        LOGGER.debug(String.format("Maps populated. Found %d sources.", sourceList.size()));
    }

}
