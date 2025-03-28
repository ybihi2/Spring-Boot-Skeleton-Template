package com.jydoc.deliverable4.security.Exceptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Exception thrown when attempting to register with an email address
 * that already exists in the system.
 *
 * <p>This exception should be thrown during user registration validation
 * when a duplicate email address is detected.</p>
 */
public class EmailExistsException extends RuntimeException {
    private static final Logger logger = LogManager.getLogger(EmailExistsException.class);

    /**
     * Constructs a new exception with a standardized message format.
     *
     * @param email the duplicate email address that caused the exception
     */
    public EmailExistsException(String email) {
        super(String.format("The email address '%s' is already registered", email));
        logger.warn("Registration attempt with existing email: {}", email);
    }

    /**
     * Constructs a new exception with custom message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public EmailExistsException(String message, Throwable cause) {
        super(message, cause);
        logger.warn("Email conflict detected: {}", message, cause);
    }

    /**
     * Gets the duplicate email that caused this exception.
     *
     * @return the duplicate email address
     */
    public String getEmail() {
        return extractEmailFromMessage(getMessage());
    }

    private String extractEmailFromMessage(String message) {
        if (message != null && message.contains("'")) {
            return message.substring(message.indexOf("'") + 1, message.lastIndexOf("'"));
        }
        return "unknown";
    }
}