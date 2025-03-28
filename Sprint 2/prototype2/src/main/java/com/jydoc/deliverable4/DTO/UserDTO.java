package com.jydoc.deliverable4.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for user registration and updates.
 * <p>
 * Contains validation rules for user input with appropriate error messages.
 * Represents the minimum required data for user operations.
 */
@Data
public class UserDTO {

    /**
     * Unique username for the user.
     * Must be 3-20 characters long.
     */
    @NotEmpty(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be 3-20 characters")
    private String username;

    /**
     * Secure password for the user account.
     * Must contain at least:
     * - 6 characters
     * - One uppercase letter
     * - One lowercase letter
     * - One number
     */
    @NotEmpty(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase, lowercase letter and number"
    )
    private String password;

    /**
     * User's email address.
     * Must be in valid email format.
     */
    @Email(message = "Invalid email format")
    @NotEmpty(message = "Email is required")
    private String email;

    /**
     * User's first name.
     * Cannot be empty.
     */
    @NotEmpty(message = "First name is required")
    private String firstName;

    /**
     * User's last name.
     * Cannot be empty.
     */
    @NotEmpty(message = "Last name is required")
    private String lastName;

    /**
     * Default authority/role assigned to the user.
     * Defaults to 'ROLE_USER' if not specified.
     */
    private String authority = "ROLE_USER";
}