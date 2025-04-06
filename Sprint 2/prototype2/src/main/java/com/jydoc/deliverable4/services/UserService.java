package com.jydoc.deliverable4.services;

import com.jydoc.deliverable4.model.UserModel;
import com.jydoc.deliverable4.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service handling user management operations including:
 * - User retrieval by various criteria
 * - User existence checks
 * - User updates and deletions
 * - User counting and listing
 *
 * <p>All methods are transactional with appropriate read-only or read-write semantics.</p>
 *
 * <p>This service provides a clean API for user-related operations while abstracting
 * repository access and ensuring proper transaction management.</p>
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    private final UserRepository userRepository;

    /* ====================== User Retrieval Methods ====================== */

    /**
     * Finds an active user by username or email (case-insensitive).
     *
     * @param usernameOrEmail The username or email to search for (whitespace trimmed)
     * @return Optional containing the found active user, or empty if not found or inactive
     */
    @Transactional(readOnly = true)
    public Optional<UserModel> findActiveUser(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail.trim().toLowerCase())
                .filter(UserModel::isEnabled);
    }

    /**
     * Retrieves a user by their exact username (case-sensitive).
     *
     * @param username The exact username to search for
     * @return Found UserModel entity
     * @throws IllegalArgumentException if no user found with given username
     */
    @Transactional(readOnly = true)
    public UserModel findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id The user's unique ID
     * @return Optional containing the found user, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<UserModel> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Retrieves all users in the system.
     *
     * @return List of all UserModel entities, empty list if none exist
     */
    @Transactional(readOnly = true)
    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

    /* ====================== User Status Methods ====================== */

    /**
     * Checks if a user exists with the given ID.
     *
     * @param id The user ID to check
     * @return true if a user exists with this ID, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    /**
     * Gets the total count of users in the system.
     *
     * @return The number of users
     */
    @Transactional(readOnly = true)
    public long getUserCount() {
        return userRepository.count();
    }

    /* ====================== User Modification Methods ====================== */

    /**
     * Updates an existing user.
     *
     * <p>Note: The entire user entity will be updated. For partial updates,
     * consider adding dedicated methods.</p>
     *
     * @param user The user entity to update
     * @throws IllegalArgumentException if the user is null
     */
    @Transactional
    public void updateUser(UserModel user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        userRepository.save(user);
        logger.debug("Updated user with ID: {}", user.getId());
    }

    /**
     * Deletes a user by their unique identifier.
     *
     * @param id The ID of the user to delete
     * @throws IllegalArgumentException if no user exists with this ID
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
        logger.debug("Deleted user with ID: {}", id);
    }
}