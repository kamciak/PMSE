package com.publicationmetasearchengine.management.usermanagement;

import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.services.mailingservice.exception.EmailNotSentException;
import com.publicationmetasearchengine.dao.users.exceptions.InvalidCredentialsException;
import com.publicationmetasearchengine.dao.users.exceptions.UserAlreadyExistsException;
import com.publicationmetasearchengine.dao.users.exceptions.UserDoesNotExistException;

public interface UserManager {
    User getUserByLoginAndPassword(String login, String password) throws InvalidCredentialsException;

    void createUser(User user, String password) throws UserAlreadyExistsException;
    /**
     * nie zmienia loginu ani hasła
     * @param user
     */
    void updateUserInformation(User user) throws UserDoesNotExistException;
    void updateUserPassword(User user, String newPassword) throws UserDoesNotExistException;
    void deleteUser(User user) throws UserDoesNotExistException;

    void remindPassword(String email) throws UserDoesNotExistException, EmailNotSentException;
}
