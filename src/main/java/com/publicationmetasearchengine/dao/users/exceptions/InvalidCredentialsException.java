package com.publicationmetasearchengine.dao.users.exceptions;

public class InvalidCredentialsException extends Exception{
    private static final long serialVersionUID = 1L;

    public InvalidCredentialsException() {
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(Throwable cause) {
        super(cause);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
