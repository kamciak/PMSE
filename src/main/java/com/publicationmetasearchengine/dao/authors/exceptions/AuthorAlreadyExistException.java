package com.publicationmetasearchengine.dao.authors.exceptions;

public class AuthorAlreadyExistException extends Exception{
    private static final long serialVersionUID = 1L;

    public AuthorAlreadyExistException() {
    }

    public AuthorAlreadyExistException(String message) {
        super(message);
    }
}
