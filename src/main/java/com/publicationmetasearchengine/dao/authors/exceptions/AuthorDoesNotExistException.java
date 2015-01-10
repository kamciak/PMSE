package com.publicationmetasearchengine.dao.authors.exceptions;

public class AuthorDoesNotExistException extends Exception{
    private static final long serialVersionUID = 1L;

    public AuthorDoesNotExistException() {
    }

    public AuthorDoesNotExistException(String message) {
        super(message);
    }
}
