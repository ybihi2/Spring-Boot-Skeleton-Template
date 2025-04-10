package com.jydoc.deliverable4.services.userservices;

import com.jydoc.deliverable4.dtos.userdtos.UserDTO;
import com.jydoc.deliverable4.security.Exceptions.EmailExistsException;
import com.jydoc.deliverable4.security.Exceptions.UsernameExistsException;
import com.jydoc.deliverable4.repositories.userrepositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service component responsible for validating user registration data.
 * <p>
 * Performs checks for duplicate usernames and email addresses before user registration.
 * All validation methods are transactional and read-only to ensure data consistency.
 */
@Service
@RequiredArgsConstructor
public class UserValidationHelper {

    private final UserRepository userRepository;

    /**
     * Validates user registration data for potential conflicts.
     *
     * @param userDto the user data transfer object containing registration information
     * @throws IllegalArgumentException if the provided user data is null
     * @throws UsernameExistsException if the username is already registered
     * @throws EmailExistsException if the email address is already registered
     */
    @Transactional(readOnly = true)
    public void validateUserRegistration(UserDTO userDto) {
        if (userDto == null) {
            throw new IllegalArgumentException("User data cannot be null");
        }

        validateUsername(userDto.getUsername());
        validateEmail(userDto.getEmail());
    }

    /**
     * Checks if a username already exists in the system.
     *
     * @param username the username to check (will be trimmed before checking)
     * @throws UsernameExistsException if the username is already taken
     */
    @Transactional(readOnly = true)
    public void validateUsername(String username) {
        String normalizedUsername = username.trim();
        if (existsByUsername(normalizedUsername)) {
            throw new UsernameExistsException(normalizedUsername);
        }
    }

    /**
     * Checks if an email address already exists in the system.
     *
     * @param email the email to check (will be normalized to lowercase and trimmed)
     * @throws EmailExistsException if the email is already registered
     */
    @Transactional(readOnly = true)
    public void validateEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        if (existsByEmail(normalizedEmail)) {
            throw new EmailExistsException(normalizedEmail);
        }
    }

    /**
     * Checks if a username exists in the repository.
     *
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Checks if an email exists in the repository.
     *
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}