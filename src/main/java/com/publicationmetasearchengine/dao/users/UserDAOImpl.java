package com.publicationmetasearchengine.dao.users;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.dao.DBSchema;
import com.publicationmetasearchengine.dao.users.exceptions.InvalidCredentialsException;
import com.publicationmetasearchengine.dao.users.exceptions.UserAlreadyExistsException;
import com.publicationmetasearchengine.dao.users.exceptions.UserDoesNotExistException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDAOImpl implements UserDAO {
    private static final Logger LOGGER = Logger.getLogger(UserDAOImpl.class);

    private class UserRowMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new User(rs.getInt(DBSchema.USERS_ID_COLUMN.getName()),
                rs.getString(DBSchema.USERS_LOGIN_COLUMN.getName()),
                rs.getString(DBSchema.USERS_NAME_COLUMN.getName()),
                rs.getString(DBSchema.USERS_SURNAME_COLUMN.getName()),
                rs.getString(DBSchema.USERS_EMAIL_COLUMN.getName()));
        }
    }

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void insertUser(User user, String password) throws UserAlreadyExistsException{
        String query = new InsertQuery(DBSchema.USERS_TABLE)
                .addColumn(DBSchema.USERS_LOGIN_COLUMN, user.getLogin())
                .addColumn(DBSchema.USERS_PASSWORD_COLUMN, password)
                .addColumn(DBSchema.USERS_NAME_COLUMN, user.getName())
                .addColumn(DBSchema.USERS_SURNAME_COLUMN, user.getSurname())
                .addColumn(DBSchema.USERS_EMAIL_COLUMN, user.getEmail())
                .validate().toString();
        try {
            jdbcTemplate.execute(query);
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException(String.format("User [%s] already exists", user.getLogin()));
        }
    }

    @Override
    public void updateUser(User user) throws UserDoesNotExistException{
        String query = new UpdateQuery(DBSchema.USERS_TABLE)
                .addSetClause(DBSchema.USERS_NAME_COLUMN, user.getName())
                .addSetClause(DBSchema.USERS_SURNAME_COLUMN, user.getSurname())
                .addSetClause(DBSchema.USERS_EMAIL_COLUMN, user.getEmail())
                .addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.USERS_ID_COLUMN, user.getId()))
                .validate().toString();
        if (jdbcTemplate.update(query) == 0) {
            throw new UserDoesNotExistException(String.format("User with ID [%d] not found", user.getId()));
        }
    }

    @Override
    public User getUserByLoginPassword(String login, String password) throws InvalidCredentialsException {
        String query = new SelectQuery()
                .addAllColumns()
                .addFromTable(DBSchema.USERS_TABLE)
                .addCondition(
                    new ComboCondition(ComboCondition.Op.AND,
                        new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.USERS_LOGIN_COLUMN, login),
                        new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.USERS_PASSWORD_COLUMN, password)
                    )
                ).validate().toString();

        try {
            return (User) jdbcTemplate.queryForObject(query, new UserRowMapper());
        } catch (EmptyResultDataAccessException ex ){
            throw new InvalidCredentialsException(String.format("User [%s] not found", login));
        }
    }

    @Override
    public User getUserById(int userId) throws UserDoesNotExistException {
        String query = new SelectQuery()
                .addAllColumns()
                .addFromTable(DBSchema.USERS_TABLE)
                .addCondition(
                        new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.USERS_ID_COLUMN, userId)
                ).validate().toString();

        try {
            return (User) jdbcTemplate.queryForObject(query, new UserRowMapper());
        } catch (EmptyResultDataAccessException ex ){
            throw new UserDoesNotExistException(String.format("User with Id [%d] not found", userId));
        }
    }

    @Override
    public int getUserIdByLoginPassword(String login, String password) throws InvalidCredentialsException {
        String query = new SelectQuery()
                .addColumns(DBSchema.USERS_ID_COLUMN)
                .addFromTable(DBSchema.USERS_TABLE)
                .addCondition(
                    new ComboCondition(ComboCondition.Op.AND,
                        new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.USERS_LOGIN_COLUMN, login),
                        new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.USERS_PASSWORD_COLUMN, password)
                    )
                ).validate().toString();
        try {
            return jdbcTemplate.queryForInt(query);
        } catch (EmptyResultDataAccessException ex) {
            throw new InvalidCredentialsException(String.format("User [%s] not found", login));
        }
    }

    @Override
    public void deleteUserByUserId(int userId) throws UserDoesNotExistException {
        String query = new DeleteQuery(DBSchema.USERS_TABLE)
                .addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.USERS_ID_COLUMN, userId))
                .validate().toString();
        if (jdbcTemplate.update(query) == 0) {
            throw new UserDoesNotExistException(String.format("User with ID [%d] not found", userId));
        }
    }

    @Override
    public void changeUserPasswordByUserId(int userId, String newPassword) throws UserDoesNotExistException {
        String query = new UpdateQuery(DBSchema.USERS_TABLE)
                .addSetClause(DBSchema.USERS_PASSWORD_COLUMN, newPassword)
                .addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.USERS_ID_COLUMN, userId))
                .validate().toString();
        if (jdbcTemplate.update(query) == 0) {
            throw new UserDoesNotExistException(String.format("User with ID [%d] not found", userId));
        }
    }

    @Override
    public String getPasswordById(int userId) throws UserDoesNotExistException{
        String query = new SelectQuery()
                .addColumns(DBSchema.USERS_PASSWORD_COLUMN)
                .addCondition(
                    new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.USERS_ID_COLUMN, userId)
                ).validate().toString();
        try {
            return (String) jdbcTemplate.queryForObject(query, String.class);
        } catch (EmptyResultDataAccessException ex) {
            throw new UserDoesNotExistException(String.format("User with ID [%d] not found", userId));
        }
    }

    @Override
    public List<User> getUsersByEmail(String email) throws UserDoesNotExistException{
        String query = new SelectQuery()
                .addAllColumns()
                .addFromTable(DBSchema.USERS_TABLE)
                .addCondition(
                    new BinaryCondition(BinaryCondition.Op.EQUAL_TO, DBSchema.USERS_EMAIL_COLUMN, email)
                ).validate().toString();
        @SuppressWarnings("unchecked")
        List<User> userList = jdbcTemplate.query(query, new UserRowMapper());
        if (userList.isEmpty()) {
            throw new UserDoesNotExistException(String.format("Email [%s] not found", email));
        }
        return userList;
    }
}
