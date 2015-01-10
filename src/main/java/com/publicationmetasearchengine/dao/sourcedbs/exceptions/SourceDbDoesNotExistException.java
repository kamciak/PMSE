package com.publicationmetasearchengine.dao.sourcedbs.exceptions;

public class SourceDbDoesNotExistException extends Exception{
    private static final long serialVersionUID = 1L;

    public SourceDbDoesNotExistException() {
    }

    public SourceDbDoesNotExistException(String message) {
        super(message);
    }
}
