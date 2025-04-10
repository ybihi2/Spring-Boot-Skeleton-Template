package com.jydoc.deliverable4.security.Exceptions;

public class MedicationCreationException extends RuntimeException {
    public MedicationCreationException(String message) {
        super(message);
    }

    public MedicationCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}