package com.publicationmetasearchengine.gui.mainmenu;
import com.publicationmetasearchengine.gui.homescreen.HomeScreenPanel;
import com.publicationmetasearchengine.gui.loginscreen.LoginScreenPanel;
import javax.swing.plaf.MenuBarUI;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
public class MainMenuBarUnauthorizedUser extends MainMenuBar{

    public MainMenuBarUnauthorizedUser() {
        super();
        initHomeMenuItem();
        initLoginMenuItem();
    }
    
    private void initHomeMenuItem(){
        MenuItem homeMenuItem = addItem("News", new Command() {
            private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuItem selectedItem) {
                getApplication().getMainWindow().setContent(new HomeScreenPanel(new MainMenuBarUnauthorizedUser()));
            }
        });
    }

    private void initLoginMenuItem(){
        MenuItem loginMenuItem = addItem("Login", new Command() {
        private static final long serialVersionUID = 1L;

            @Override
            public void menuSelected(MenuItem selectedItem) {
                getApplication().getMainWindow().setContent(new LoginScreenPanel());
            }
        });
    }
}
