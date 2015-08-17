/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.dao.impactfactor.exceptions;

/**
 *
 * @author Kamciak
 */
public class JournalDoesNotExistException extends Exception{
    private static final long serialVersionUID = 1L;

    public JournalDoesNotExistException() {
    }

    public JournalDoesNotExistException(String message) {
        super(message);
    }
}
