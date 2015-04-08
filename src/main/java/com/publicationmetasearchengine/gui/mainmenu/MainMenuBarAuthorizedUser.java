package com.publicationmetasearchengine.gui.mainmenu;

import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.gui.ConfirmWindow;
import com.publicationmetasearchengine.dao.users.exceptions.UserDoesNotExistException;
import com.publicationmetasearchengine.gui.homescreen.HomeScreenPanel;
import com.publicationmetasearchengine.gui.notificationcriteriasscreen.NotificationCriteriasScreenPanel;
import com.publicationmetasearchengine.gui.profilescreen.ProfileScreenPanel;
import com.publicationmetasearchengine.gui.searchscreen.SearchScreenPanel;
import com.publicationmetasearchengine.gui.toreadscreen.ToReadScreenPanel;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
public class MainMenuBarAuthorizedUser extends MainMenuBar{


    public MainMenuBarAuthorizedUser() {
        super();
        initHomeMenuItem();
        initSearchMenuItem();
        initToReadMenuItem();
        initNotificationCriteriasMenuItem();
        initProfileMenuItem();
        initLogoutMenuItem();
    }


    private void initHomeMenuItem(){
        MenuItem homeMenuItem = addItem("News", new Command() {
            private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuItem selectedItem) {
                getApplication().getMainWindow().setContent(user.getScreenPanel(new HomeScreenPanel()));
            }
        });
    }

    private void initToReadMenuItem(){
        MenuItem toReadMenuItem = addItem("To-Read", new Command() {
            private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuItem selectedItem) {
                getApplication().getMainWindow().setContent(user.getScreenPanel(new ToReadScreenPanel()));
            }
        });

    }

    private void initSearchMenuItem() {
        MenuItem searchMenuItem = addItem("Search", new Command() {
            private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuItem selectedItem) {
                getApplication().getMainWindow().setContent(user.getScreenPanel(new SearchScreenPanel()));
            }
        });

    }

    private void initNotificationCriteriasMenuItem() {
        MenuItem notificationCriteriasMenuItem = addItem("Notification Criterias", new Command() {
            private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuItem selectedItem) {
                getApplication().getMainWindow().setContent(user.getScreenPanel(new NotificationCriteriasScreenPanel()));
            }
        });

    }

    private void initProfileMenuItem(){
        MenuItem profileMenuItem = addItem("Profile", null);
        profileMenuItem.addItem("Edit", new Command() {
            private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuItem selectedItem) {
                getApplication().getMainWindow().setContent(user.getScreenPanel(new ProfileScreenPanel()));
            }
        });
        profileMenuItem.addSeparator();
        profileMenuItem.addItem("Delete", new Command() {
            private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuItem selectedItem) {
                final User user = (User) getApplication().getUser();
                new ConfirmWindow(getApplication(), "Confirmation", "Do you really want to delete your profile?") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void yesButtonClick() {
                        try {
                            userManager.deleteUser(user);
                            logout();
                        } catch (UserDoesNotExistException ex) {
                            LOGGER.fatal("Error while deleteing user [%s] - Should not occure");
                        }
                    }
                };
            }
        });
    }

    private void initLogoutMenuItem(){
        MenuItem logoutMenuItem = addItem("Logout", new Command() {
            private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuItem selectedItem) {
                logout();
            }
        });
    }
}
