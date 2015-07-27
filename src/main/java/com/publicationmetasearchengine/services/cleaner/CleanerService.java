/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.services.cleaner;

import com.publicationmetasearchengine.dao.properties.PropertiesManager;
import com.publicationmetasearchengine.management.publicationmanagement.PublicationManager;
import com.publicationmetasearchengine.services.ServiceJobProvider;
import java.io.Serializable;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.Job;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.newTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author Kamciak
 */
public class CleanerService implements ServiceJobProvider, Serializable{
    
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(CleanerService.class);


    @Configurable(preConstruction = true)
    public static class CleanerServiceJob implements Job {
        @Autowired
        private PublicationManager publicationManager;
        private int maxYearsOfPublication;
        
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            publicationManager.removePublicationBeforeDate(new DateTime().minusYears(maxYearsOfPublication));
        }
        
        public void setMaxYearsOfPublication(int maxYearsOfPublication) {
            this.maxYearsOfPublication = maxYearsOfPublication;
        }
    }
    
    
    private int maxYearsOfPublication;
    private String schedule;
    
    
    
    @Override
    public void initialize(String settingsPrefix) {
        final PropertiesManager pm = PropertiesManager.getInstance();
        schedule = pm.getProperty(settingsPrefix+"schedule");
        maxYearsOfPublication = Integer.parseInt(pm.getProperty(settingsPrefix + "maxYearsOfPublication", "10"));

        LOGGER.info("Schedule = " + schedule);
        LOGGER.info("Max days of publication = " + maxYearsOfPublication);
        LOGGER.info("Initialized");
    }
    
    @Override
    public JobDetail getJobDetail() {
        return newJob(CleanerService.CleanerServiceJob.class)
                .withDescription("CleanerService Job")
                .usingJobData("maxYearsOfPublication", maxYearsOfPublication)
                .build();
    }

    @Override
    public Trigger getTrigger() {
        return newTrigger()
                .withIdentity("CleanerService Job", "Cleaner Service")
                .withSchedule(cronSchedule(schedule))
                .build();
    }
    
}
