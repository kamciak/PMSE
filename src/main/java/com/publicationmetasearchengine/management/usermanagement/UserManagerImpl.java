package com.publicationmetasearchengine.management.usermanagement;

import com.publicationmetasearchengine.dao.publications.PublicationDAO;
import com.publicationmetasearchengine.dao.publications.exceptions.RelationDoesNotExistException;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.dao.users.UserDAO;
import com.publicationmetasearchengine.services.mailingservice.MailingService;
import com.publicationmetasearchengine.services.mailingservice.emailparts.EmailSubject;
import com.publicationmetasearchengine.services.mailingservice.emailparts.PasswordRecoveryEmailBody;
import com.publicationmetasearchengine.services.mailingservice.exception.EmailNotSentException;
import com.publicationmetasearchengine.dao.users.exceptions.InvalidCredentialsException;
import com.publicationmetasearchengine.dao.users.exceptions.UserAlreadyExistsException;
import com.publicationmetasearchengine.dao.users.exceptions.UserDoesNotExistException;
import com.publicationmetasearchengine.utils.CryptoUtils;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

@Component
@Configurable(preConstruction = true)
public class UserManagerImpl implements UserManager{
    private static final Logger LOGGER = Logger.getLogger(UserManagerImpl.class);

    @Autowired
    private UserDAO userDAO;
    @Autowired
    private MailingService mailingService;
    @Autowired
    private PublicationDAO publicationDAO;

    @Override
    public User getUserByLoginAndPassword(String login, String password) throws InvalidCredentialsException {
        User user = userDAO.getUserByLoginPassword(login, password);
        LOGGER.debug(String.format("User [%s] requested", user.toString()));
        return user;
    }

    @Override
    public void createUser(User user, String password) throws UserAlreadyExistsException {
        userDAO.insertUser(user, password);
        LOGGER.info(String.format("User [%s] created", user.toString()));
    }

    @Override
    public void updateUserInformation(User user) throws UserDoesNotExistException {
        userDAO.updateUser(user);
        LOGGER.info(String.format("Information about user [%s] updated", user.toString()));
    }

    @Override
    public void updateUserPassword(User user, String newPassword) throws UserDoesNotExistException {
        userDAO.changeUserPasswordByUserId(user.getId(), newPassword);
        LOGGER.info(String.format("Users [%s] pasword changed", user.toString()));
    }

    @Override
    public void deleteUser(User user) throws UserDoesNotExistException {
        try {
            publicationDAO.removeUserPublications(user.getId());
        } catch (RelationDoesNotExistException ex) {
            LOGGER.info(String.format("User [%s] without publications marked to read", user.toString()));
        }
        userDAO.deleteUserByUserId(user.getId());
        LOGGER.info(String.format("User [%s] deleted", user.toString()));
    }

    @Override
    public void remindPassword(String email) throws UserDoesNotExistException, EmailNotSentException {
        List<User> users = userDAO.getUsersByEmail(email);
        for (User user: users)
            mailingService.createEmail(EmailSubject.PASSWORD_RECOVER)
                    .setToAddress(user.getEmail())
                    .setBody(new PasswordRecoveryEmailBody(user.getName(), user.getLogin(),
                        CryptoUtils.decrypt(userDAO.getPasswordById(user.getId()))))
                    .send();
    }

}
