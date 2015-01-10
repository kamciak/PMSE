package com.publicationmetasearchengine.dao.publications.exceptions;

public class PublicationAlreadyExistException extends Exception{
    private static final long serialVersionUID = 1L;

    public PublicationAlreadyExistException() {
    }

    public PublicationAlreadyExistException(String message) {
        super(message);
    }
}
