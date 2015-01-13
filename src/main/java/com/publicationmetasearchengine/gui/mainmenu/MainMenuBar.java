package com.publicationmetasearchengine.gui.mainmenu;

import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.gui.homescreen.HomeScreenPanel;
import com.publicationmetasearchengine.management.usermanagement.UserManager;
import com.vaadin.ui.MenuBar;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author Kamciak
 */
public abstract class MainMenuBar extends MenuBar {
    protected static final long serialVersionUID = 1L;
    protected static final Logger LOGGER = Logger.getLogger(MainMenuBarUnauthorizedUser.class);

    @Autowired
    protected UserManager userManager;
    protected User user;

    public MainMenuBar() {
        setWidth("100%");
    }

    @Override
    public void attach() {
        super.attach();
        user = (User) getApplication().getUser();
    }


    protected void logout() {
        getApplication().setUser(null);
        getApplication().getMainWindow().setContent(new HomeScreenPanel(new MainMenuBarUnauthorizedUser()));
        LOGGER.info(String.format("User [%s] logged out", user.toString()));
    }
}

