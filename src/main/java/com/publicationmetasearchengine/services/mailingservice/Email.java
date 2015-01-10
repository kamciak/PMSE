package com.publicationmetasearchengine.services.mailingservice;

import com.publicationmetasearchengine.services.mailingservice.emailparts.EmailBody;
import com.publicationmetasearchengine.services.mailingservice.emailparts.EmailSubject;
import com.publicationmetasearchengine.services.mailingservice.exception.EmailNotSentException;

public class Email {
    private final MailingService mailingService;

    private String toAddress;
    private EmailSubject subject;
    private EmailBody body;

    Email(MailingService mailingService, EmailSubject subject) {
        this.mailingService = mailingService;
        this.subject = subject;
    }

    String getToAddress() {
        return toAddress;
    }

    EmailSubject getSubject() {
        return subject;
    }

    EmailBody getBody() {
        return body;
    }

    public Email setToAddress(String toAddress) {
        this.toAddress = toAddress;
        return this;
    }

    public Email setBody(EmailBody body) {
        this.body = body;
        return this;
    }

    public void send() throws EmailNotSentException {
        mailingService.sendEmail(this);
    }

    @Override
    public String toString() {
        return String.format("To: %s\nSubject: %s\n%s", toAddress, subject.getSubject(), body);
    }
}
