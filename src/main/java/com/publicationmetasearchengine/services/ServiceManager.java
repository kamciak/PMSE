package com.publicationmetasearchengine.services;

import com.publicationmetasearchengine.dao.properties.PropertiesManager;
import com.publicationmetasearchengine.services.cleaner.CleanerService;
import com.publicationmetasearchengine.services.croneservice.CroneService;
import com.publicationmetasearchengine.services.notificationservice.NotificationService;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ServiceManager implements ServletContextListener{
    private static final Logger LOGGER = Logger.getLogger(ServiceManager.class);

    private static final String COLLECTORS_ARRAY = "datacollectors";
    private static final String SETTINGS_SEPARATOR = ".";
    private static final String NOTIFICATION_SERVICE_PREFIX = "notificationservice" + SETTINGS_SEPARATOR;
    private static final String CLEANER_SERVICE_PREFIX = "cleanerservice" + SETTINGS_SEPARATOR;
    private static final String COLLECTOR_PREFIX = "datacollector" + SETTINGS_SEPARATOR;
    private static final String CLASS_SETTING = "class";
    
    private final CroneService croneService;

    public ServiceManager() {
        croneService = new CroneService();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext())
                                    .getAutowireCapableBeanFactory()
                                    .autowireBean(this);

        LOGGER.info("Initializing services");
        final PropertiesManager pm = PropertiesManager.getInstance();
        final String collectorsString = pm.getProperty(COLLECTORS_ARRAY);
        if (!collectorsString.isEmpty()) {
            final List<String> collectors = Arrays.asList(collectorsString.split(","));
            LOGGER.debug(String.format("Collectors to add: %s", collectors.toString()));

            for (String collectorName : collectors) {
                final String collectorPrefix = COLLECTOR_PREFIX + collectorName.trim() + SETTINGS_SEPARATOR;
                final String classPath = pm.getProperty(collectorPrefix + CLASS_SETTING);
                try {
                    ServiceJobProvider dataCollector = (ServiceJobProvider) Class.forName(classPath).newInstance();
                    dataCollector.initialize(collectorPrefix);
                    LOGGER.debug("Starting class " + classPath);         
                    croneService.scheduleJob(dataCollector.getJobDetail(), dataCollector.getTrigger());
                } catch (ClassNotFoundException ex) {
                    LOGGER.fatal(ex);
                } catch (InstantiationException ex) {
                    LOGGER.fatal(ex);
                } catch (IllegalAccessException ex) {
                    LOGGER.fatal(ex);
                }
            }
        }

        NotificationService notificationService = new NotificationService();
        notificationService.initialize(NOTIFICATION_SERVICE_PREFIX);
        if (pm.getProperty(NOTIFICATION_SERVICE_PREFIX+"enabled", "0").equals("1"))
            croneService.scheduleJob(notificationService.getJobDetail(), notificationService.getTrigger());
        else
            LOGGER.info("Notificaton Service Disabled");
        
        CleanerService cleanerService = new CleanerService();
        cleanerService.initialize(CLEANER_SERVICE_PREFIX);
        if (pm.getProperty(CLEANER_SERVICE_PREFIX+"enabled", "0").equals("1"))
            croneService.scheduleJob(cleanerService.getJobDetail(), cleanerService.getTrigger());
        else
            LOGGER.info("Cleaner Service Disabled");

        if (croneService.getJobCout() > 0)
            croneService.start();
        else
            LOGGER.warn("Crone service has no job. There is no need to start it.");
        LOGGER.info("Initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (croneService.isRunning())
            croneService.stop();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex){
        }
    }
}
