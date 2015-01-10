package com.publicationmetasearchengine.services.mailingservice.exception;

public class EmailNotSentException extends Exception{
    private static final long serialVersionUID = 1L;

    public EmailNotSentException() {
    }

    public EmailNotSentException(String message) {
        super(message);
    }

    public EmailNotSentException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailNotSentException(Throwable cause) {
        super(cause);
    }

    public EmailNotSentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
