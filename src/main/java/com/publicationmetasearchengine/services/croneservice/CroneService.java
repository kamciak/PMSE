package com.publicationmetasearchengine.services.croneservice;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class CroneService {

    private static final Logger LOGGER = Logger.getLogger(CroneService.class);

    private Scheduler scheduler;

    private boolean isRunning = false;
    private int jobCouter = 0;

    public CroneService() {
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            LOGGER.info("Initialized");
        } catch (SchedulerException ex) {
            LOGGER.fatal("Could not create scheduler", ex);
        }
    }

    public void start() {
        try {
            scheduler.start();
            isRunning = true;
            LOGGER.info("Started");
        } catch (SchedulerException ex){
            LOGGER.fatal("Could not start scheduler", ex);
        }
    }

    public void stop() {
        try {
            scheduler.shutdown(true);
            Thread.sleep(5000);
            isRunning = false;
            LOGGER.info("Stoped");
        } catch (Exception ex){
            LOGGER.fatal("Could not shutdown scheduler", ex);
        }
    }

    public void scheduleJob(JobDetail job, Trigger trigger) {
        try {
            scheduler.scheduleJob(job, trigger);
            ++jobCouter;
            LOGGER.info(String.format("Job %s added", job.getDescription()));
        } catch (SchedulerException ex){
            LOGGER.fatal("Could not schedule job", ex);
        }
    }

    public int getJobCout() {
        return jobCouter;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
