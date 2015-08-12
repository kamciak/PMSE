package com.publicationmetasearchengine;

import com.publicationmetasearchengine.utils.Notificator;
import com.vaadin.terminal.Terminal;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.vaadin.navigator7.NavigableApplication;
import org.vaadin.navigator7.window.NavigableAppLevelWindow;


public class PMSENavigableApplication extends NavigableApplication {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PMSENavigableApplication.class);

    public PMSENavigableApplication() {
        setTheme("pmseTheme");
        initLog4j();
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
    
    @Override
    public Object getUser()
    {
        return super.getUser();
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

    @Override
    public NavigableAppLevelWindow createNewNavigableAppLevelWindow() {
        return new PMSEAppLevelWindow();
    }
    

    public static SystemMessages getSystemMessages() {
    CustomizedSystemMessages m = new CustomizedSystemMessages();
    m.setSessionExpiredURL("/");
    
    return m;
}
    
}