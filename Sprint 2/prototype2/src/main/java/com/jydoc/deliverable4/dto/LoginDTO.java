package com.jydoc.deliverable4.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;

/**
 * Data Transfer Object (DTO) for user login requests.
 * <p>
 * Validates credentials format and provides utility methods for credential processing.
 *
 * @param username The username or email for authentication (case-insensitive)
 * @param password The password for authentication
 */
public record LoginDTO(
        @NotBlank(message = "Username or email cannot be blank")
        String username,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an empty LoginDTO instance.
     * @return A LoginDTO with empty strings for both fields
     */
    public static LoginDTO empty() {
        return new LoginDTO("", "");
    }

    /**
     * Returns a normalized version of the username (trimmed and lowercase).
     * @return The processed username ready for comparison
     */
    public String getNormalizedUsername() {
        return username.trim().toLowerCase();
    }

    /**
     * Checks if this LoginDTO represents an empty credential set.
     * @return true if both username and password are blank
     */
    public boolean isEmpty() {
        return username.isBlank() && password.isBlank();
    }
}