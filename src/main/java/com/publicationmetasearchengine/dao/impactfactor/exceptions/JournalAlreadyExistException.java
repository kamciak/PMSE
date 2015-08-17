/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.dao.impactfactor.exceptions;

/**
 *
 * @author Kamciak
 */
public class JournalAlreadyExistException extends Exception{
    private static final long serialVersionUID = 1L;

    public JournalAlreadyExistException() {
    }

    public JournalAlreadyExistException(String message) {
        super(message);
    }
}
