package com.publicationmetasearchengine.dao.publications.exceptions;

public class PublicationDoesNotExistException extends Exception{
    private static final long serialVersionUID = 1L;

    public PublicationDoesNotExistException() {
    }

    public PublicationDoesNotExistException(String message) {
        super(message);
    }
}
