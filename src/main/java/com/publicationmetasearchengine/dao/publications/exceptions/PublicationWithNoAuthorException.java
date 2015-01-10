package com.publicationmetasearchengine.dao.publications.exceptions;

public class PublicationWithNoAuthorException extends Exception{
    private static final long serialVersionUID = 1L;

    public PublicationWithNoAuthorException() {
    }

    public PublicationWithNoAuthorException(String message) {
        super(message);
    }
}
