package com.publicationmetasearchengine.dao.filtercriterias.exceptions;

public class FilterCriteriasDoesNotExistException extends Exception{
    private static final long serialVersionUID = 1L;

    public FilterCriteriasDoesNotExistException() {
    }

    public FilterCriteriasDoesNotExistException(String message) {
        super(message);
    }

    public FilterCriteriasDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public FilterCriteriasDoesNotExistException(Throwable cause) {
        super(cause);
    }

    public FilterCriteriasDoesNotExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
