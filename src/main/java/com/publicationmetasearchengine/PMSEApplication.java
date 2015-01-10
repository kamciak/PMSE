package com.publicationmetasearchengine;

import com.publicationmetasearchengine.gui.loginscreen.LoginScreenPanel;
import com.publicationmetasearchengine.utils.Notificator;
import com.vaadin.Application;
import com.vaadin.terminal.Terminal;
import com.vaadin.ui.Window;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class PMSEApplication extends Application{
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(PMSEApplication.class);

    @Override
    public void init() {
        initLog4j();
        initMainWindow();
        LOGGER.info("Publication MetaSearch Engine is up");
    }

    private void initMainWindow() {
        setTheme("pmseTheme");
        Window mainWindow = new Window("Publication MetaSearch Engine");
        mainWindow.setContent(new LoginScreenPanel());
        setMainWindow(mainWindow);
    }

    @Override
    public void terminalError(Terminal.ErrorEvent event) {
        if (event.getThrowable().getCause() instanceof UnsupportedOperationException) {
            Notificator.showNotification(this, "Error", "This functionality is not yet supported.", Notificator.NotificationType.WARNING);
        } else {
            Notificator.showNotification(this, "Error", "Unexpected error.\nPlease notify administrator.", Notificator.NotificationType.ERROR);
        }
        LOGGER.fatal(String.format("Uncaught exception - %s", event.getThrowable().getClass()), event.getThrowable());
    }

    private void initLog4j() {
        Properties log = new Properties();
        try {
            log.load(this.getClass().getClassLoader().getResourceAsStream("log4j.properties"));
        } catch (IOException e) {
            System.err.println("Error reading Log4j properies");
            e.printStackTrace();
        }
        PropertyConfigurator.configure(log);
    }
}