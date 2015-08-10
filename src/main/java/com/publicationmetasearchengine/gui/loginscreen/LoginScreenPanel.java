package com.publicationmetasearchengine.gui.loginscreen;

import com.publicationmetasearchengine.PMSENavigableApplication;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.gui.homescreen.HomeScreenPanel;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.gui.pmsecomponents.PMSEPanel;
import com.publicationmetasearchengine.management.usermanagement.UserManager;
import com.publicationmetasearchengine.dao.users.exceptions.InvalidCredentialsException;
import com.publicationmetasearchengine.gui.ScreenPanel;
import com.publicationmetasearchengine.utils.CryptoUtils;
import com.publicationmetasearchengine.utils.Notificator;
import com.vaadin.event.ShortcutAction;
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
import org.vaadin.navigator7.Page;

@Page(uriName="LoginScreenPanel")
@SuppressWarnings("serial")
@Configurable(preConstruction = true)
public class LoginScreenPanel extends CustomComponent implements ScreenPanel {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(LoginScreenPanel.class);

    @Autowired
    private UserManager userManager;

    private PMSEPanel loginPanel= new PMSEPanel("Login");

    private TextField usernameField = new TextField();
    private PasswordField passwordField = new PasswordField();

    private Label usernameLabel = new Label("Username");
    private Label passwordLabel = new Label("Password");

    private PMSEButton loginBtn = new PMSEButton("Login");
    private PMSEButton forgotPasswordBtn = new PMSEButton("Forgot your password?");
    private PMSEButton registerBtn = new PMSEButton("Register");
    private PMSEButton goBackBtn = new PMSEButton("Go back");
    
    public LoginScreenPanel() {
        setSizeUndefined();
        initLoginPanel();

        HorizontalLayout hl = new HorizontalLayout();
        hl.setMargin(true);
        hl.setSpacing(true);
        hl.setWidth("100%");
        hl.addComponent(loginPanel);
        hl.setComponentAlignment(loginPanel, Alignment.MIDDLE_CENTER);
        setCompositionRoot(hl);
    }

    private void initLoginPanel() {
        GridLayout gl = new GridLayout(2, 5);
        gl.setSpacing(true);
        gl.setMargin(true);

        gl.addComponent(usernameLabel,      0, 0);
        gl.addComponent(usernameField,      1, 0);
        gl.addComponent(passwordLabel,      0, 1);
        gl.addComponent(passwordField,      1, 1);
        VerticalLayout buttonLayout = initButtonLayout();
        gl.addComponent(buttonLayout, 1, 3);
        gl.setComponentAlignment(buttonLayout, Alignment.MIDDLE_RIGHT);
        gl.addComponent(goBackBtn, 0, 4);
        gl.setComponentAlignment(goBackBtn, Alignment.MIDDLE_LEFT);
        usernameField.setWidth("100%");
        passwordField.setWidth("100%");

        usernameField.addStyleName("text-alignment-center");
        passwordField.addStyleName("text-alignment-center");

        usernameField.setTabIndex(1);
        passwordField.setTabIndex(2);
        loginBtn.setTabIndex(3);
        loginBtn.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        forgotPasswordBtn.setTabIndex(-1);
        registerBtn.setTabIndex(-1);

        loginPanel.setContent(gl);
        loginPanel.setSizeUndefined();

        initGoBackBtnListener();
    }

    private VerticalLayout initButtonLayout() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(true);

        loginBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                login();
            }
        });

        forgotPasswordBtn.setStyleName("link");
        forgotPasswordBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                new ForgottenPasswordWindow(getApplication());
            }
        });

        registerBtn.setStyleName("link");
        registerBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                new RegisterWindow(getApplication());
            }
        });

        vl.addComponent(loginBtn);
        vl.addComponent(forgotPasswordBtn);
        vl.addComponent(registerBtn);
        vl.setComponentAlignment(loginBtn, Alignment.MIDDLE_RIGHT);
        vl.setComponentAlignment(forgotPasswordBtn, Alignment.MIDDLE_RIGHT);
        vl.setComponentAlignment(registerBtn, Alignment.MIDDLE_RIGHT);
        
        return vl;
    }

    private void login(){
        String username = (String) usernameField.getValue();
        String password = CryptoUtils.encrypt((String) passwordField.getValue());
        try {
            User user = userManager.getUserByLoginAndPassword(username, password);
            getApplication().setUser(user);
            LOGGER.info(String.format("User [%s] logged in", user));
            PMSENavigableApplication.getCurrentNavigableAppLevelWindow().getNavigator().navigateTo(HomeScreenPanel.class);       
        } catch (InvalidCredentialsException ex) {
            Notificator.showNotification(getApplication(), "Login error", "Invalid user credentials", Notificator.NotificationType.ERROR);
            LOGGER.debug(String.format("Invalid users credentials [%s - %s]", username, password));
        }
    }

    private void initGoBackBtnListener() {
        goBackBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event) {
                PMSENavigableApplication.getCurrentNavigableAppLevelWindow().getNavigator().navigateTo(HomeScreenPanel.class);
            }
        });
    }
}
