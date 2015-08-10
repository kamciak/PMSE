package com.publicationmetasearchengine.gui.profilescreen;

import com.publicationmetasearchengine.PMSENavigableApplication;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.dao.users.exceptions.UserDoesNotExistException;
import com.publicationmetasearchengine.gui.ScreenPanel;
import com.publicationmetasearchengine.gui.mainmenu.MainMenuBarAuthorizedUser;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.management.usermanagement.UserManager;
import com.publicationmetasearchengine.utils.CryptoUtils;
import com.publicationmetasearchengine.utils.Notificator;
import com.publicationmetasearchengine.utils.PMSEConstants;
import com.publicationmetasearchengine.utils.validable.Validable;
import com.publicationmetasearchengine.utils.validable.ValidationException;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
public class ProfileScreenPanel extends CustomComponent implements ScreenPanel {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ProfileScreenPanel.class);
    private static final String NOTIFICATOR_CAPTION = "Profile editor";

    private static enum ValidatorType {
        PASSWORD_CHANGE_VALIDATOR,
        PROFILE_CHANGE_VALIDATOR;
    }

    private  class Validator implements Validable {

        private ValidatorType validatorType;

        public Validator(ValidatorType validatorType) {
            this.validatorType = validatorType;
        }

        @Override
        public void validate() throws ValidationException {
            switch (validatorType){
                case PASSWORD_CHANGE_VALIDATOR: validatePassword(); break;
                case PROFILE_CHANGE_VALIDATOR: validateProfile(); break;
            }
        }

        private void validateProfile() throws ValidationException {
            String email = getFieldValue(emailField);
            if (email == null || email.isEmpty())
                throw new ValidationException("Email must not be empty.");
            if (!email.matches(PMSEConstants.EMAIL_VALIDATION_STRING))
                throw new ValidationException("Given email address is not valid.");
        }

        private void validatePassword() throws ValidationException {
            String password = getFieldValue(passwordField);
            String confirmPassword = getFieldValue(confirmPasswordField);
            if (password == null || password.isEmpty())
                throw new ValidationException("Password must not be empty.");
            if (!password.equals(confirmPassword))
                throw new ValidationException("Passwords do not match.");

        }
    }

    //private MainMenuBarAuthorizedUser menuBar = new MainMenuBarAuthorizedUser(PMSENavigableApplication.getCurrentNavigableAppLevelWindow().getNavigator());

    @Autowired
    UserManager userManager;

    private PMSEPanel profilePanel = new PMSEPanel();
    private TextField nameField = new TextField();
    private TextField surnameField = new TextField();
    private TextField emailField = new TextField();
    private Label nameLabel = new Label("Name");
    private Label surnameLabel = new Label("Surname");
    private Label emailLabel = new Label("Email");
    private PMSEButton saveBtn = new PMSEButton("Save");

    private PMSEPanel changePasswordPanel = new PMSEPanel("Change password");
    private PasswordField passwordField = new PasswordField();
    private PasswordField confirmPasswordField = new PasswordField();
    private Label passwordLabel = new Label("New password");
    private Label confirmPasswordLabel = new Label("Confirm password");
    private PMSEButton changePasswordBtn = new PMSEButton("Change");

    public ProfileScreenPanel() {
        setSizeUndefined();
        setWidth("100%");
//        setMargin(true);
//        setSpacing(true);

        initProfilePanel();
        initPasswordPanel();
        //addComponent(menuBar);

        VerticalLayout vl = new VerticalLayout();
     //   addComponent(vl);
        vl.setSizeUndefined();
        vl.addComponent(profilePanel);
        vl.addComponent(changePasswordPanel);
        
        

        HorizontalLayout hl = new HorizontalLayout();
        hl.setMargin(true);
        hl.setSpacing(true);
        hl.setWidth("100%");
        hl.addComponent(vl);
        hl.setComponentAlignment(vl, Alignment.MIDDLE_CENTER);
        setCompositionRoot(hl);
//        setComponentAlignment(vl, Alignment.MIDDLE_CENTER);
    }

    @Override
    public void attach() {
        super.attach();
        User user = (User) getApplication().getUser();
        profilePanel.setCaption(String.format("Profile: %s", user.getLogin()));
        fillupProfilePanel(user);
    }

    private void initProfilePanel() {
        GridLayout gl = new GridLayout(2, 4);
        gl.setSpacing(true);
        gl.setMargin(true);
        gl.setSizeFull();

        gl.addComponent(nameLabel,    0, 0);
        gl.addComponent(nameField,    1, 0);
        gl.addComponent(surnameLabel, 0, 1);
        gl.addComponent(surnameField, 1, 1);
        gl.addComponent(emailLabel,   0, 2);
        gl.addComponent(emailField,   1, 2);
        gl.addComponent(saveBtn,      1, 3);

        gl.setComponentAlignment(saveBtn, Alignment.MIDDLE_RIGHT);
        gl.setColumnExpandRatio(0, 1);
        gl.setColumnExpandRatio(1, 0);

        nameField.addStyleName("text-alignment-center");
        surnameField.addStyleName("text-alignment-center");
        emailField.addStyleName("text-alignment-center");

        saveBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                try {
                    new Validator(ValidatorType.PROFILE_CHANGE_VALIDATOR).validate();
                    User oldUser = (User) getApplication().getUser();
                    String name = getFieldValue(nameField);
                    String surname = getFieldValue(surnameField);
                    String email = getFieldValue(emailField);
                    User newUser = new User(oldUser.getId(), oldUser.getLogin(), name, surname, email);
                    LOGGER.debug(String.format("Updating information about user [%s] -> [%s]", oldUser.toString(), newUser.toString()));
                    try {
                        userManager.updateUserInformation(newUser);
                    } catch (UserDoesNotExistException ex) {
                        LOGGER.fatal("Should not occure", ex);
                        throw new ValidationException("Internal error.\nPlease try again later.");
                    }
                    getApplication().setUser(newUser);
                    Notificator.showNotification(getApplication(), NOTIFICATOR_CAPTION, "Informations have been changed", Notificator.NotificationType.HUMANIZED);
                    LOGGER.info(String.format("User [%s] informations updated", oldUser.toString()));
                } catch (ValidationException ex) {
                    Notificator.showNotification(getApplication(), NOTIFICATOR_CAPTION, ex.getMessage(), Notificator.NotificationType.ERROR);
                }
            }
        });

        profilePanel.setContent(gl);
        profilePanel.setSizeFull();
    }

    private void initPasswordPanel() {
        GridLayout gl = new GridLayout(2, 3);
        gl.setSpacing(true);
        gl.setMargin(true);

        gl.addComponent(passwordLabel,        0, 0);
        gl.addComponent(passwordField,        1, 0);
        gl.addComponent(confirmPasswordLabel, 0, 1);
        gl.addComponent(confirmPasswordField, 1, 1);
        gl.addComponent(changePasswordBtn,            1, 2);

        gl.setComponentAlignment(changePasswordBtn, Alignment.MIDDLE_RIGHT);

        passwordField.addStyleName("text-alignment-center");
        confirmPasswordField.addStyleName("text-alignment-center");

        changePasswordBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                try {
                    new Validator(ValidatorType.PASSWORD_CHANGE_VALIDATOR).validate();
                    try {
                        userManager.updateUserPassword((User)getApplication().getUser(),CryptoUtils.encrypt(getFieldValue(passwordField)));
                    } catch (UserDoesNotExistException ex) {
                        LOGGER.fatal("Should not occure", ex);
                        throw new ValidationException("Internal error.\nPlease try again later.");
                    }
                    Notificator.showNotification(getApplication(), NOTIFICATOR_CAPTION, "Password has been changed", Notificator.NotificationType.HUMANIZED);
                    passwordField.setValue("");
                    confirmPasswordField.setValue("");
                } catch (ValidationException ex) {
                    Notificator.showNotification(getApplication(), NOTIFICATOR_CAPTION, ex.getMessage(), Notificator.NotificationType.ERROR);
                }
            }
        });

        changePasswordPanel.setContent(gl);
        changePasswordPanel.setSizeUndefined();
    }

    private void fillupProfilePanel(User user) {
        setFieldValue(nameField, user.getName());
        setFieldValue(surnameField, user.getSurname());
        setFieldValue(emailField, user.getEmail());
    }

    private void setFieldValue(TextField field, String value) {
        if (value == null || value.equals(""))
            field.setValue("");
        else
            field.setValue(value);
    }

    private String getFieldValue(AbstractTextField field) {
        String value = (String) field.getValue();
        if (value == null | value.equals(""))
            return null;
        return value;
    }
}
