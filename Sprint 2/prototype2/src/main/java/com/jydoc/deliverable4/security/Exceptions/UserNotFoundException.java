package com.jydoc.deliverable4.security.Exceptions;

/**
 * Exception thrown when a requested user cannot be found in the system.
 *
 * <p>This exception typically occurs when attempting to retrieve, update, or delete
 * a user with a specific ID that doesn't exist in the database.</p>
 *
 * <p>The exception includes the ID that was searched for in its error message,
 * making it easier to diagnose the issue during debugging.</p>
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Constructs a new UserNotFoundException with a detailed message containing
     * the ID that couldn't be found.
     *
     * @param id The user ID that could not be found in the system. The ID will
     *           be included in the exception's detail message.
     * @throws NullPointerException if the provided id is null (though primitive
     *                              long can't be null, this note is included for documentation completeness)
     */
    public UserNotFoundException(Long id) {
        super("Could not find user with id: " + id);
    }
}