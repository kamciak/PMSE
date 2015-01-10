package com.publicationmetasearchengine.gui.loginscreen;

import com.publicationmetasearchengine.gui.pmsecomponents.PMSEButton;
import com.publicationmetasearchengine.services.mailingservice.exception.EmailNotSentException;
import com.publicationmetasearchengine.management.usermanagement.UserManager;
import com.publicationmetasearchengine.dao.users.exceptions.UserDoesNotExistException;
import com.publicationmetasearchengine.utils.Notificator;
import com.publicationmetasearchengine.utils.PMSEConstants;
import com.publicationmetasearchengine.utils.validable.Validable;
import com.publicationmetasearchengine.utils.validable.ValidationException;
import com.vaadin.Application;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.Serializable;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
public class ForgottenPasswordWindow implements Serializable, Validable{
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ForgottenPasswordWindow.class);
    private static final String NOTIFICATOR_CAPTION = "Password reminder";

    private Window window = new Window(NOTIFICATOR_CAPTION);

    private TextField emailField = new TextField();
    private Label emailLabel = new Label("Enter your email address:");
    private PMSEButton okBtn = new PMSEButton("Ok");
    private PMSEButton cancelBtn = new PMSEButton("Cancel");
    private final Application application;

    @Autowired
    private UserManager userManager;

    public ForgottenPasswordWindow(Application application) {
        this.application = application;
        window.setModal(true);
        window.setResizable(false);
        window.setClosable(false);
        window.setSizeUndefined();

        VerticalLayout hl = new VerticalLayout();
        hl.setSpacing(true);
        hl.setMargin(true);
        hl.setSizeUndefined();
        hl.addComponent(emailLabel);
        hl.addComponent(emailField);

        HorizontalLayout buttonLayout = initButtonLayout();
        hl.addComponent(buttonLayout);

        hl.setComponentAlignment(buttonLayout, Alignment.MIDDLE_RIGHT);

        emailField.addStyleName("text-alignment-right");
        window.setContent(hl);
        application.getMainWindow().getWindow().addWindow(window);
    }

    private HorizontalLayout initButtonLayout() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);

        okBtn.setDisableOnClick(true);
        okBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    validate();
                    lockButtons(true);
                    try {
                        userManager.remindPassword((String) emailField.getValue());
                    } catch (UserDoesNotExistException ex) {
                        throw new ValidationException("Given email does not exists in our database");
                    } catch (EmailNotSentException ex){
                        LOGGER.error("Email not sent", ex);
                        throw new ValidationException("Email could not be sent.\nPlease try again later");
                    }
                    window.getParent().removeWindow(window);
                    Notificator.showNotification(application, NOTIFICATOR_CAPTION, "Password has been sent.", Notificator.NotificationType.HUMANIZED);
                } catch (ValidationException ex) {
                    Notificator.showNotification(application, NOTIFICATOR_CAPTION, ex.getMessage(), Notificator.NotificationType.ERROR);
                    lockButtons(false);
                }
            }
        });
        cancelBtn.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                window.getParent().removeWindow(window);
            }
        });

        hl.addComponent(okBtn);
        hl.addComponent(cancelBtn);

        return hl;
    }

    @Override
    public void validate() throws ValidationException {
        String email = (String) emailField.getValue();
        if (email == null || email.isEmpty())
            throw new ValidationException("Email must not be empty.");
        if (!email.matches(PMSEConstants.EMAIL_VALIDATION_STRING))
            throw new ValidationException("Given email is invalid.");
    }

    private void lockButtons(boolean lock){
        okBtn.setEnabled(!lock);
        cancelBtn.setEnabled(!lock);
    }
}
