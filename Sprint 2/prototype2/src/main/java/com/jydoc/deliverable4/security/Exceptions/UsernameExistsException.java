package com.jydoc.deliverable4.security.Exceptions;

/**
 * Custom exception thrown when attempting to create or update a user with a username
 * that already exists in the system.
 *
 * <p>This exception should be used during user registration or profile updates
 * to enforce unique username constraints.
 */
public class UsernameExistsException extends RuntimeException {

    /**
     * Constructs a new UsernameExistsException with the specified detail message.
     *
     * @param message the detail message that explains which username already exists.
     *               The message is saved for later retrieval by the {@link #getMessage()} method.
     */
    public UsernameExistsException(String message) {
        super(message);
    }

    /**
     * Constructs a new UsernameExistsException with the specified detail message and cause.
     *
     * @param message the detail message that explains which username already exists
     * @param cause the underlying cause of this exception
     */
    public UsernameExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}