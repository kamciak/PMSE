package com.publicationmetasearchengine.gui.mainmenu;

import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.gui.homescreen.HomeScreenPanel;
import com.publicationmetasearchengine.management.usermanagement.UserManager;
import com.vaadin.ui.MenuBar;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.navigator7.Navigator;

/**
 *
 * @author Kamciak
 */
public class MainMenuBar extends MenuBar {
    protected static final long serialVersionUID = 1L;
    protected static final Logger LOGGER = Logger.getLogger(MainMenuBar.class);
    protected Navigator navigator;
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

    public void setNavigator(Navigator navigator)
    {
        this.navigator = navigator;
    }
    
    protected void logout() {
        getApplication().setUser(null);
        navigator.navigateTo(HomeScreenPanel.class);
        LOGGER.info(String.format("User [%s] logged out", user.toString()));
    }
}

