package com.jydoc.deliverable4.security.Exceptions;

public class WeakPasswordException extends RuntimeException {
    public WeakPasswordException(String message) {
        super(message);
    }
}
