package com.jydoc.deliverable4.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for user-related operations.
 * This class represents the user data that is transferred between layers,
 * particularly between the controller and service layers.
 *
 * <p>Includes validation constraints for user input fields to ensure data integrity
 * before processing. Uses Lombok annotations to reduce boilerplate code for getters/setters.</p>
 *
 *
 * @version 1.0
 * @see com.jydoc.deliverable4.model.UserModel
 * @since 1.0
 */
@Setter
@Getter
@Data
public class UserDTO {

    /**
     * The username for authentication. Must be unique and between 3-20 characters.
     *
     * @NotBlank Ensures the username is not null or empty
     * @Size Constrains the length between 3-20 characters
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be 3-20 characters")
    private String username;

    /**
     * The password for authentication. Must meet complexity requirements.
     *
     * @NotBlank Ensures the password is not null or empty
     * @Size Requires minimum 6 characters
     * @Pattern Enforces at least one uppercase, one lowercase letter and one number
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase, lowercase letter and number"
    )
    private String password;

    /**
     * The user's email address. Must be in valid format.
     *
     * @NotBlank Ensures the email is not null or empty
     * @Pattern Validates the email format using regex pattern
     */
    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
            "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$",
            message = "Invalid email format")
    private String email;

    /**
     * The user's first name. Cannot be blank.
     */
    @NotBlank(message = "First name is required")
    private String firstName;

    /**
     * The user's last name. Cannot be blank.
     */
    @NotBlank(message = "Last name is required")
    private String lastName;

    /**
     * The authority/role assigned to the user. Defaults to "ROLE_USER".
     */
    private String authority = "ROLE_USER";

    /**
     * Default constructor.
     */
    public UserDTO() {
    }

    /**
     * Constructs a UserDTO with specified parameters.
     *
     * @param username the username
     * @param password the password
     * @param email the email address
     * @param firstName the first name
     * @param lastName the last name
     */
    public UserDTO(String username, String password, String email, String firstName, String lastName) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}