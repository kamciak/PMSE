package com.publicationmetasearchengine.dao.users;

import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.dao.users.exceptions.InvalidCredentialsException;
import com.publicationmetasearchengine.dao.users.exceptions.UserAlreadyExistsException;
import com.publicationmetasearchengine.dao.users.exceptions.UserDoesNotExistException;
import java.util.List;

public interface UserDAO {
    void insertUser(User user, String password) throws UserAlreadyExistsException;
    /**
     * nie zmienia loginu ani hasła
     * @param user
     */
    void updateUser(User user) throws UserDoesNotExistException;

    User getUserByLoginPassword(String login, String password) throws InvalidCredentialsException;
    User getUserById(int userId) throws UserDoesNotExistException;
    int getUserIdByLoginPassword(String login, String password) throws InvalidCredentialsException;

    void deleteUserByUserId(int userId) throws UserDoesNotExistException;
    void changeUserPasswordByUserId(int userId, String newPassword) throws UserDoesNotExistException;
    String getPasswordById(int userId) throws UserDoesNotExistException;

    List<User> getUsersByEmail(String email) throws UserDoesNotExistException;
}
