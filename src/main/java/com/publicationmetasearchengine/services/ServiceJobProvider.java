package com.publicationmetasearchengine.services;

import org.quartz.JobDetail;
import org.quartz.Trigger;

public interface ServiceJobProvider {

    void initialize(String settingsPrefix);

    JobDetail getJobDetail();
    Trigger getTrigger();
}
