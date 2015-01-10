package com.publicationmetasearchengine.services.mailingservice;

import com.publicationmetasearchengine.services.mailingservice.emailparts.EmailSubject;
import com.publicationmetasearchengine.services.mailingservice.exception.EmailNotSentException;

public interface MailingService {
    Email createEmail(EmailSubject subject);

    void sendEmail(Email email) throws EmailNotSentException;
}
