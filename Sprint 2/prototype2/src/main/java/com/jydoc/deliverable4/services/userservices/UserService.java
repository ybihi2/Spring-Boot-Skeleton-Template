package com.jydoc.deliverable4.services.userservices;

import com.jydoc.deliverable4.dtos.userdtos.UserDTO;
import com.jydoc.deliverable4.model.UserModel;
import com.jydoc.deliverable4.repositories.userrepositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service handling comprehensive user management operations including:
 * - User retrieval by various criteria (ID, username, email)
 * - User existence verification and counting
 * - User profile updates and account deletions
 * - Password management and verification
 * - Conversion between entity and DTO representations
 *
 * <p>All operations are transactional with appropriate read-only or read-write semantics.
 * Security-sensitive operations include proper validation and password handling.</p>
 *
 * <p>This service provides a clean API for user-related operations while abstracting
 * repository access and ensuring proper transaction management and audit logging.</p>
 *
 * <p>Logging is implemented at different levels:
 * - TRACE: Detailed flow tracing
 * - DEBUG: Important variable states and minor events
 * - INFO: Significant business operations
 * - WARN: Unexpected but handled situations
 * - ERROR: Critical failures</p>
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /* ====================== User Retrieval Methods ====================== */

    /**
     * Finds an active user by username or email.
     *
     * @param usernameOrEmail The username or email to search for (case-insensitive)
     * @return Optional containing the user if found and active, empty otherwise
     * @throws IllegalArgumentException if input is null or empty after trimming
     */
    @Transactional(readOnly = true)
    public Optional<UserModel> findActiveUser(String usernameOrEmail) {
        logger.trace("Entering findActiveUser with parameter: {}", usernameOrEmail);

        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            logger.warn("Attempted to find user with null/empty usernameOrEmail");
            throw new IllegalArgumentException("Username or email cannot be null or empty");
        }

        String normalizedInput = usernameOrEmail.trim().toLowerCase();
        logger.debug("Searching for active user with normalized input: {}", normalizedInput);

        Optional<UserModel> user = userRepository.findByUsernameOrEmail(normalizedInput)
                .filter(UserModel::isEnabled);

        if (user.isPresent()) {
            logger.debug("Found active user: {}", user.get().getUsername());
        } else {
            logger.debug("No active user found for: {}", normalizedInput);
        }

        return user;
    }

    /**
     * Finds a user by username (exact match, case-sensitive).
     *
     * @param username The username to search for
     * @return The found user entity
     * @throws IllegalArgumentException if user is not found
     */
    @Transactional(readOnly = true)
    public UserModel findByUsername(String username) {
        logger.trace("Entering findByUsername with parameter: {}", username);

        if (username == null || username.trim().isEmpty()) {
            logger.warn("Attempted to find user with null/empty username");
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        logger.debug("Searching for user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found with username: {}", username);
                    return new IllegalArgumentException("User not found");
                });
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param id The user ID to search for
     * @return Optional containing the user if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<UserModel> getUserById(Long id) {
        logger.trace("Entering getUserById with parameter: {}", id);

        if (id == null || id <= 0) {
            logger.warn("Attempted to get user with invalid ID: {}", id);
            return Optional.empty();
        }

        logger.debug("Retrieving user by ID: {}", id);
        return userRepository.findById(id);
    }

    /**
     * Retrieves all users in the system.
     *
     * @return List of all user entities
     */
    @Transactional(readOnly = true)
    public List<UserModel> getAllUsers() {
        logger.trace("Entering getAllUsers");
        logger.debug("Retrieving all users from repository");

        List<UserModel> users = userRepository.findAll();
        logger.info("Retrieved {} users from database", users.size());

        return users;
    }

    /**
     * Verifies if the provided password matches the user's current password.
     *
     * @param username The username of the user to verify
     * @param currentPassword The password to verify
     * @return true if passwords match, false otherwise
     * @throws IllegalArgumentException if user is not found
     */
    @Transactional(readOnly = true)
    public boolean verifyCurrentPassword(String username, String currentPassword) {
        logger.trace("Entering verifyCurrentPassword for user: {}", username);

        if (currentPassword == null || currentPassword.isEmpty()) {
            logger.warn("Attempted password verification with empty password for user: {}", username);
            return false;
        }

        UserModel user = findByUsername(username);
        boolean matches = passwordEncoder.matches(currentPassword, user.getPassword());

        logger.debug("Password verification {} for user: {}",
                matches ? "succeeded" : "failed", username);

        return matches;
    }

    /* ====================== User Status Methods ====================== */

    /**
     * Checks if a user exists with the given ID.
     *
     * @param id The user ID to check
     * @return true if user exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        logger.trace("Entering existsById with parameter: {}", id);

        if (id == null || id <= 0) {
            logger.debug("Exists check for invalid ID: {}", id);
            return false;
        }

        boolean exists = userRepository.existsById(id);
        logger.debug("User existence check for ID {}: {}", id, exists);

        return exists;
    }

    /**
     * Gets the total count of users in the system.
     *
     * @return The number of users
     */
    @Transactional(readOnly = true)
    public long getUserCount() {
        logger.trace("Entering getUserCount");

        long count = userRepository.count();
        logger.info("System currently contains {} users", count);

        return count;
    }

    /* ====================== User Modification Methods ====================== */

    /**
     * Updates a user entity in the database.
     *
     * @param user The user entity to update
     * @throws IllegalArgumentException if user is null
     */
    @Transactional
    public void updateUser(UserModel user) {
        logger.trace("Entering updateUser");

        if (user == null) {
            logger.error("Attempted to update null user");
            throw new IllegalArgumentException("User cannot be null");
        }

        logger.debug("Updating user with ID: {}", user.getId());
        userRepository.save(user);
        logger.info("Successfully updated user with ID: {}", user.getId());
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to delete
     * @throws IllegalArgumentException if user is not found
     */
    @Transactional
    public void deleteUser(Long id) {
        logger.trace("Entering deleteUser with parameter: {}", id);

        if (!userRepository.existsById(id)) {
            logger.error("Attempted to delete non-existent user with ID: {}", id);
            throw new IllegalArgumentException("User not found with ID: " + id);
        }

        logger.debug("Deleting user with ID: {}", id);
        userRepository.deleteById(id);
        logger.info("Successfully deleted user with ID: {}", id);
    }

    /* ====================== Profile Management Methods ====================== */

    /**
     * Retrieves a user's profile data as a DTO by username.
     *
     * @param username The username to search for
     * @return UserDTO containing profile information
     * @throws IllegalArgumentException if user is not found
     */
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        logger.trace("Entering getUserByUsername with parameter: {}", username);

        UserModel user = findByUsername(username);
        UserDTO dto = convertToDTO(user);

        logger.debug("Returning DTO for user: {}", username);
        return dto;
    }

    /**
     * Updates a user's profile information.
     *
     * @param username The username of the user to update
     * @param userDTO The DTO containing updated profile data
     * @return The updated user DTO
     * @throws IllegalArgumentException if DTO is null or email is already in use
     */
    @Transactional
    public UserDTO updateUserProfile(String username, UserDTO userDTO) {
        logger.trace("Entering updateUserProfile for user: {}", username);

        Objects.requireNonNull(userDTO, "UserDTO cannot be null");
        logger.debug("Updating profile for user: {} with data: {}", username, userDTO);

        UserModel user = findByUsername(username);

        // Verify email uniqueness if changing email
        if (!user.getEmail().equalsIgnoreCase(userDTO.getEmail())) {
            logger.debug("Email change detected for user: {}", username);
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                logger.warn("Email already in use: {}", userDTO.getEmail());
                throw new IllegalArgumentException("Email already in use");
            }
        }

        // Update allowed fields
        user.setFirstName(userDTO.getFirstName().trim());
        user.setLastName(userDTO.getLastName().trim());
        user.setEmail(userDTO.getEmail().trim().toLowerCase());

        UserModel updatedUser = userRepository.save(user);
        logger.info("Successfully updated profile for user: {}", username);

        return convertToDTO(updatedUser);
    }

    /**
     * Changes a user's password after verifying the current password.
     *
     * @param username The username of the user
     * @param currentPassword The current password for verification
     * @param newPassword The new password to set
     * @return true if password was changed successfully, false if verification failed
     */
    @Transactional
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        logger.trace("Entering changePassword for user: {}", username);

        UserModel user = findByUsername(username);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            logger.warn("Password change failed for user: {} - current password mismatch", username);
            return false;
        }

        // Set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password successfully changed for user: {}", username);

        return true;
    }

    /**
     * Deletes a user account after password verification.
     *
     * @param username The username of the account to delete
     * @param password The password for verification
     * @return true if account was deleted, false if verification failed
     */
    @Transactional
    public boolean deleteAccount(String username, String password) {
        logger.trace("Entering deleteAccount for user: {}", username);

        UserModel user = findByUsername(username);

        // Verify password before deletion
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Account deletion failed for user: {} - password verification failed", username);
            return false;
        }

        userRepository.delete(user);
        logger.info("Account successfully deleted for user: {}", username);

        return true;
    }

    /* ====================== Helper Methods ====================== */

    /**
     * Converts a UserModel entity to a UserDTO.
     *
     * @param user The user entity to convert
     * @return The converted DTO
     * @throws IllegalArgumentException if user is null
     */
    private UserDTO convertToDTO(UserModel user) {
        logger.trace("Entering convertToDTO for user: {}", user != null ? user.getUsername() : "null");

        if (user == null) {
            logger.error("Attempted to convert null user to DTO");
            throw new IllegalArgumentException("User cannot be null");
        }

        UserDTO dto = new UserDTO();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());

        logger.debug("Converted user {} to DTO", user.getUsername());
        return dto;
    }
}