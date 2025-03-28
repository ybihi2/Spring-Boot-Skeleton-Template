package com.jydoc.deliverable4.security.Exceptions;

public class UsernameExistsException extends RuntimeException {
    public UsernameExistsException(String message) {
        super(message);
    }
}