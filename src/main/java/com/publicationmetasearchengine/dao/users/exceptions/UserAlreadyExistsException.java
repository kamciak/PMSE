package com.publicationmetasearchengine.dao.users.exceptions;

public class UserAlreadyExistsException extends Exception{
    private static final long serialVersionUID = 1L;

    public UserAlreadyExistsException() {
    }

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(Throwable cause) {
        super(cause);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
