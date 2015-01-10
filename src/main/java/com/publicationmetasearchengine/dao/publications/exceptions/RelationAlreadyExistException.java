package com.publicationmetasearchengine.dao.publications.exceptions;

public class RelationAlreadyExistException extends Exception{
    private static final long serialVersionUID = 1L;

    public RelationAlreadyExistException() {
    }

    public RelationAlreadyExistException(String message) {
        super(message);
    }
}
