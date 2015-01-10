package com.publicationmetasearchengine.services.notificationservice;

import com.publicationmetasearchengine.dao.filtercriterias.FilterCriteriaDAO;
import com.publicationmetasearchengine.dao.filtercriterias.exceptions.FilterCriteriasDoesNotExistException;
import com.publicationmetasearchengine.dao.properties.PropertiesManager;
import com.publicationmetasearchengine.dao.publications.exceptions.RelationAlreadyExistException;
import com.publicationmetasearchengine.dao.users.UserDAO;
import com.publicationmetasearchengine.dao.users.exceptions.UserDoesNotExistException;
import com.publicationmetasearchengine.data.Publication;
import com.publicationmetasearchengine.data.User;
import com.publicationmetasearchengine.data.filters.NamedFilterCriteria;
import com.publicationmetasearchengine.management.publicationmanagement.PublicationManager;
import com.publicationmetasearchengine.services.ServiceJobProvider;
import com.publicationmetasearchengine.services.mailingservice.MailingService;
import com.publicationmetasearchengine.services.mailingservice.emailparts.EmailSubject;
import com.publicationmetasearchengine.services.mailingservice.emailparts.NewNotificationEmailBody;
import com.publicationmetasearchengine.services.mailingservice.exception.EmailNotSentException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.CronScheduleBuilder.*;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

public class NotificationService implements ServiceJobProvider, Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class);


    @Configurable(preConstruction = true)
    public static class NotificationServiceJob implements Job {

        @Autowired
        private FilterCriteriaDAO filterCriteriaDAO;
        @Autowired
        private PublicationManager publicationManager;
        @Autowired
        private UserDAO userDAO;
        @Autowired
        private MailingService mailingService;

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            LOGGER.info("NotificationService started");
            final List<NamedFilterCriteria> filterCriterias = filterCriteriaDAO.getAllFiltersCriterias();
            LOGGER.debug(String.format("Found %d criterias", filterCriterias.size()));
            for(NamedFilterCriteria nfc : filterCriterias) {
                final List<Publication> publications = publicationManager.getPublicationsMatchingFiltersCriteria(nfc.getFilters(), nfc.getLastSearchDate());
                LOGGER.info(String.format("Found %d publications for criteria %s", publications.size(), nfc));
                try {
                    if (!publications.isEmpty()) {
                        Set<String> titles = new HashSet<String>();
                        User user = userDAO.getUserById(nfc.getOwnerId());
                        LOGGER.debug(String.format("User [%s] requested from database", user));
                        for(Publication publication : publications) {
                            publicationManager.insertUserPublication(user, publication);
                            titles.add(publication.getTitle());
                        }
                        LOGGER.info(String.format("Sending notification to user [%s] sent", user.getLogin()));
                        mailingService.createEmail(EmailSubject.PUBLICATION_MATCHING_CRITERIA).setToAddress(user.getEmail())
                                .setBody(new NewNotificationEmailBody(user.getName(), user.getLogin(),nfc.getName(), titles)).send();
                    }
                    filterCriteriaDAO.touchFilterCriteriasById(nfc.getId());
                } catch (UserDoesNotExistException ex) {
                    LOGGER.warn(String.format("Owner of criteria [%s] does not exists... deleting criteria", nfc));
                    try {
                        filterCriteriaDAO.deleteFilterCriteriasByID(nfc.getId());
                    } catch (FilterCriteriasDoesNotExistException ex1) {
                        LOGGER.fatal("Should not occure !!!", ex1);
                    }
                } catch (RelationAlreadyExistException ex) {
                } catch (FilterCriteriasDoesNotExistException ex) {
                    LOGGER.fatal("Should not occure !!!", ex);
                } catch (EmailNotSentException ex) {
                    LOGGER.fatal(ex);
                }
            }
            LOGGER.info("Notification Service ended");
        }
    }

    private String schedule;

    @Override
    public void initialize(String settingsPrefix) {
        final PropertiesManager pm = PropertiesManager.getInstance();
        schedule = pm.getProperty(settingsPrefix+"schedule");

        LOGGER.info("Schedule = " + schedule);
        LOGGER.info("Initialized");
    }

    @Override
    public JobDetail getJobDetail() {
        return newJob(NotificationServiceJob.class)
                .withDescription("NotificationService")
                .build();
    }

    @Override
    public Trigger getTrigger() {
        return newTrigger()
                .withIdentity("Notification Service", "Notification Service")
                .withSchedule(cronSchedule(schedule))
                .build();
    }

}
