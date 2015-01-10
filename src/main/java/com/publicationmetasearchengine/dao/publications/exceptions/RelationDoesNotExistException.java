package com.publicationmetasearchengine.dao.publications.exceptions;

public class RelationDoesNotExistException extends Exception{
    private static final long serialVersionUID = 1L;

    public RelationDoesNotExistException() {
    }

    public RelationDoesNotExistException(String message) {
        super(message);
    }
}
