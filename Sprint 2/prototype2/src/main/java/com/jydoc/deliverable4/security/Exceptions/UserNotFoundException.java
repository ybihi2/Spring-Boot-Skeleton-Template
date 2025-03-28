package com.jydoc.deliverable4.exceptions;

/**
 * Exception thrown when a user cannot be found in the system.
 */
public class UserNotFoundException extends RuntimeException {
    private final Long userId;

    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}