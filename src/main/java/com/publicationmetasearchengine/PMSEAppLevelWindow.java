/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine;

import com.publicationmetasearchengine.dao.impactfactor.ImpactFactorDAO;
import com.publicationmetasearchengine.dao.users.exceptions.UserDoesNotExistException;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.gui.ConfirmWindow;
import com.publicationmetasearchengine.gui.homescreen.HomeScreenPanel;
import com.publicationmetasearchengine.gui.loginscreen.LoginScreenPanel;
import com.publicationmetasearchengine.gui.mainmenu.MainMenuBar;
import com.publicationmetasearchengine.gui.notificationcriteriasscreen.NotificationCriteriasScreenPanel;
import com.publicationmetasearchengine.gui.profilescreen.ProfileScreenPanel;
import com.publicationmetasearchengine.gui.searchjournalscreen.SearchJournalScreenPanel;
import com.publicationmetasearchengine.gui.searchscreen.SearchScreenPanel;
import com.publicationmetasearchengine.gui.toreadscreen.ToReadScreenPanel;
import com.publicationmetasearchengine.management.usermanagement.UserManager;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.navigator7.window.HeaderFooterFluidAppLevelWindow;

/**
 *
 * @author Kamciak
 */
@Configurable(preConstruction = true)
public class PMSEAppLevelWindow extends HeaderFooterFluidAppLevelWindow {

    protected static final Logger LOGGER = Logger.getLogger(PMSEAppLevelWindow.class);
    @Autowired
    private UserManager userManager;
    private MenuBar menuBar = new MainMenuBar();
    @Autowired
    ImpactFactorDAO impactFactorDao;

    @Override
    protected Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addStyleName("header");
        header.setWidth("100%");
        header.setSpacing(false);
        header.setHeight("100%");
        addComponent(header);
        header.addComponent(menuBar);
        header.setComponentAlignment(menuBar, Alignment.TOP_LEFT);
        initUnauthorizedMenuBar();

        return header;

    }

    @Override
    protected Component createFooter() {
        VerticalLayout vl = new VerticalLayout();
        vl.setWidth("100%");
        vl.setHeight("10%");
        Label info = new Label("Publication Meta Search Engine 2014-2015");
        info.setWidth(null);
        vl.addComponent(info);
        vl.setComponentAlignment(info, Alignment.TOP_CENTER);

        return vl;
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public void setMenuBar(MenuBar menu) {
        this.menuBar = menu;
    }

    @Override
    protected ComponentContainer createComponents() {
        ComponentContainer result = super.createComponents();
        result.setSizeFull();
        return result;
    }

    public void initUnauthorizedMenuBar() {
        menuBar.removeItems();
        initHomeMenuItem();
        initLoginMenuItem();
    }

    public void initAuthorizedMenuBar() {
        menuBar.removeItems();
        initHomeMenuItem();
        initToReadMenuItem();
        initSearchMenuItem();
        initSearchJournalMenuItem();
        initNotificationCriteriasMenuItem();
        initProfileMenuItem();
        initLogoutMenuItem();
    }

    private void initHomeMenuItem() {
        MenuBar.MenuItem homeMenuItem = menuBar.addItem("News", new MenuBar.Command() {
            private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                navigator.navigateTo(HomeScreenPanel.class);
            }
        });
    }

    private void initLoginMenuItem() {
        MenuBar.MenuItem loginMenuItem = menuBar.addItem("Login", new MenuBar.Command() {
            private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                navigator.navigateTo(LoginScreenPanel.class);
            }
        });
    }
    /*
     private void initDownloadImpactFactor() {
     MenuBar.MenuItem downloadImpactFactor = menuBar.addItem("Download ImpactFactor", new MenuBar.Command() {
     private static final long serialVersionUID = 1L;

     @Override
     public void menuSelected(MenuBar.MenuItem selectedItem) {
     downloadImpactFactor();
     navigator.navigateTo(HomeScreenPanel.class);
     }
     });
     }
    
     private void downloadImpactFactor()
     {
     ImpactFactorDownloader impacFactorDownloader = new ImpactFactorDownloader();
     impacFactorDownloader.downloadImpactFactor();
     List<Journal> journalsStartAt = impactFactorDao.getJournalsStartAt('Z');
     LOGGER.debug("Journals starts at Z:");
     for(Journal journal : journalsStartAt)
     {
     LOGGER.debug(journal);
     }
     }*/

    private void initToReadMenuItem() {
        MenuBar.MenuItem toReadMenuItem = menuBar.addItem("To-Read", new MenuBar.Command() {
            private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                navigator.navigateTo(ToReadScreenPanel.class);
            }
        });

    }

    private void initSearchMenuItem() {
        MenuBar.MenuItem searchMenuItem = menuBar.addItem("Search", new MenuBar.Command() {
            private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                navigator.navigateTo(SearchScreenPanel.class);
            }
        });

    }

    private void initSearchJournalMenuItem() {
        MenuBar.MenuItem searchMenuItem = menuBar.addItem("Search Journal", new MenuBar.Command() {
            private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                navigator.navigateTo(SearchJournalScreenPanel.class);
            }
        });

    }

    private void initNotificationCriteriasMenuItem() {
        MenuBar.MenuItem notificationCriteriasMenuItem = menuBar.addItem("Notification Criterias", new MenuBar.Command() {
            private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                navigator.navigateTo(NotificationCriteriasScreenPanel.class);
            }
        });

    }

    private void initProfileMenuItem() {
        MenuBar.MenuItem profileMenuItem = menuBar.addItem("Profile", null);
        profileMenuItem.addItem("Edit", new MenuBar.Command() {
            private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                navigator.navigateTo(ProfileScreenPanel.class);
            }
        });
        profileMenuItem.addSeparator();
        profileMenuItem.addItem("Delete", new MenuBar.Command() {
            private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                new ConfirmWindow(getApplication(), "Confirmation", "Do you really want to delete your profile?") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void yesButtonClick() {
                        try {
                            userManager.deleteUser((User) PMSENavigableApplication.getCurrentNavigableAppLevelWindow().getApplication().getUser());
                            logout();
                        } catch (UserDoesNotExistException ex) {
                            LOGGER.fatal("Error while deleteing user [%s] - Should not occure");
                        }
                    }
                };
            }
        });
    }

    private void initLogoutMenuItem() {
        MenuBar.MenuItem logoutMenuItem = menuBar.addItem("Logout", new MenuBar.Command() {
            private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                logout();
            }
        });
    }

    protected void logout() {
        User user = (User) PMSENavigableApplication.getCurrentNavigableAppLevelWindow().getApplication().getUser();
        LOGGER.info(String.format("User [%s] logged out", user.toString()));
        PMSENavigableApplication.getCurrentNavigableAppLevelWindow().getApplication().setUser(null);
        navigator.navigateTo(HomeScreenPanel.class);

    }
}
