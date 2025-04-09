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
 * Service handling user management operations including:
 * - User retrieval by various criteria
 * - User existence checks
 * - User updates and deletions
 * - User counting and listing
 * - Profile management
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
    private final PasswordEncoder passwordEncoder;  // Added for password handling

    /* ====================== User Retrieval Methods ====================== */

    @Transactional(readOnly = true)
    public Optional<UserModel> findActiveUser(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail.trim().toLowerCase())
                .filter(UserModel::isEnabled);
    }

    @Transactional(readOnly = true)
    public UserModel findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional(readOnly = true)
    public Optional<UserModel> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean verifyCurrentPassword(String username, String currentPassword) {
        UserModel user = findByUsername(username);
        return passwordEncoder.matches(currentPassword, user.getPassword());
    }

    /* ====================== User Status Methods ====================== */

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public long getUserCount() {
        return userRepository.count();
    }

    /* ====================== User Modification Methods ====================== */

    @Transactional
    public void updateUser(UserModel user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        userRepository.save(user);
        logger.debug("Updated user with ID: {}", user.getId());
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
        logger.debug("Deleted user with ID: {}", id);
    }

    /* ====================== Profile Management Methods ====================== */

    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        UserModel user = findByUsername(username);
        return convertToDTO(user);
    }


    @Transactional
    public UserDTO updateUserProfile(String username, UserDTO userDTO) {
        Objects.requireNonNull(userDTO, "UserDTO cannot be null");

        UserModel user = findByUsername(username);

        // Verify email uniqueness if changing email
        if (!user.getEmail().equalsIgnoreCase(userDTO.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
        }

        // Update allowed fields
        user.setFirstName(userDTO.getFirstName().trim());
        user.setLastName(userDTO.getLastName().trim());
        user.setEmail(userDTO.getEmail().trim().toLowerCase());

        UserModel updatedUser = userRepository.save(user);
        logger.info("Updated profile for user: {}", username);
        return convertToDTO(updatedUser);
    }


    @Transactional
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        UserModel user = findByUsername(username);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            logger.debug("Password verification failed for user: {}", username);
            return false;
        }

        // Set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.debug("Password changed successfully for user: {}", username);
        return true;
    }

    @Transactional
    public boolean deleteAccount(String username, String password) {
        UserModel user = findByUsername(username);

        // Verify password before deletion
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.debug("Password verification failed for account deletion: {}", username);
            return false;
        }

        userRepository.delete(user);
        logger.info("Account deleted for user: {}", username);
        return true;
    }

    /* ====================== Helper Methods ====================== */

    private UserDTO convertToDTO(UserModel user) {
        UserDTO dto = new UserDTO();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        return dto;
    }
}