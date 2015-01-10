package com.publicationmetasearchengine.services.mailingservice.emailparts;

public enum EmailSubject {
    PASSWORD_RECOVER("Password recovery request"),
    PUBLICATION_MATCHING_CRITERIA("There are some new publications matching one of your crietrias");

    private String subject;

    private EmailSubject(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }
}
