package com.publicationmetasearchengine.gui.loginscreen;

import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.management.usermanagement.UserManager;
import com.publicationmetasearchengine.dao.users.exceptions.UserAlreadyExistsException;
import com.publicationmetasearchengine.utils.CryptoUtils;
import com.publicationmetasearchengine.utils.Notificator;
import com.publicationmetasearchengine.utils.PMSEConstants;
import com.publicationmetasearchengine.utils.validable.Validable;
import com.publicationmetasearchengine.utils.validable.ValidationException;
import com.vaadin.Application;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import java.io.Serializable;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
public class RegisterWindow implements Serializable, Validable{
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(RegisterWindow.class);
    private static final String NOTIFICATOR_CAPTION = "User Manager";

    private Window window = new Window("Register new user");

    private TextField usernameField = new TextField();
    private TextField emailField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private PasswordField confirmPasswordField = new PasswordField();

    private Label usernameLabel = new Label("Username");
    private Label emailLabel = new Label("Email");
    private Label passwordLabel = new Label("Password");
    private Label confirmPasswordLabel = new Label("Confirm password");

    private PMSEButton createBtn = new PMSEButton("Create");
    private PMSEButton cancelBtn = new PMSEButton("Cancel");

    private final Application application;

    @Autowired
    private UserManager userManager;

    public RegisterWindow(Application application) {
        this.application = application;
        window.setModal(true);
        window.setResizable(false);
        window.setClosable(false);
        window.setSizeUndefined();

        GridLayout gl = new GridLayout(2, 5);
        gl.setSpacing(true);
        gl.setMargin(true);

        gl.addComponent(usernameLabel,          0, 0);
        gl.addComponent(usernameField,          1, 0);
        gl.addComponent(emailLabel,             0, 1);
        gl.addComponent(emailField,             1, 1);
        gl.addComponent(passwordLabel,          0, 2);
        gl.addComponent(passwordField,          1, 2);
        gl.addComponent(confirmPasswordLabel,   0, 3);
        gl.addComponent(confirmPasswordField,   1, 3);

        HorizontalLayout buttonLayout = initButtonLayout();
        gl.addComponent(buttonLayout, 1, 4);

        gl.setComponentAlignment(buttonLayout, Alignment.MIDDLE_RIGHT);

        usernameField.addStyleName("text-alignment-right");
        emailField.addStyleName("text-alignment-right");
        passwordField.addStyleName("text-alignment-right");
        confirmPasswordField.addStyleName("text-alignment-right");

        window.setContent(gl);

        application.getMainWindow().getWindow().addWindow(window);
    }

    private HorizontalLayout initButtonLayout() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);

        createBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                createUser();
            }
        });
        cancelBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                window.getParent().removeWindow(window);
            }
        });

        hl.addComponent(createBtn);
        hl.addComponent(cancelBtn);

        return hl;
    }

    private void createUser() {
        try {
            validate();
            String username = (String)usernameField.getValue();
            String email = (String)emailField.getValue();
            String password = CryptoUtils.encrypt((String) passwordField.getValue());

            try {
                User user = new User(null, username, null, null, email);
                userManager.createUser(user, password);
                Notificator.showNotification(application, NOTIFICATOR_CAPTION, "User created", Notificator.NotificationType.HUMANIZED);
                window.getParent().removeWindow(window);
            } catch (UserAlreadyExistsException ex) {
                LOGGER.debug(String.format("User [%s] already exists.", username));
                throw new ValidationException(String.format("User [%s] already exists", username));
            }
        } catch (ValidationException ex) {
            Notificator.showNotification(application, NOTIFICATOR_CAPTION, ex.getMessage(), Notificator.NotificationType.ERROR);
        }
    }

    @Override
    public void validate() throws ValidationException {
        String username = (String)usernameField.getValue();
        String email = (String)emailField.getValue();
        String password = (String) passwordField.getValue();
        String confirmPassword = (String) confirmPasswordField.getValue();

        if (username == null || username.isEmpty())
            throw new ValidationException("Username must not be empty.");
        if (email == null || email.isEmpty())
            throw new ValidationException("Email must not be empty.");
        if (password == null || password.isEmpty())
            throw new ValidationException("Password must not be empty.");

        if (!email.matches(PMSEConstants.EMAIL_VALIDATION_STRING))
            throw new ValidationException("Given email address is not valid.");
        if (!password.equals(confirmPassword))
            throw new ValidationException("Passwords do not match.");
    }
}
