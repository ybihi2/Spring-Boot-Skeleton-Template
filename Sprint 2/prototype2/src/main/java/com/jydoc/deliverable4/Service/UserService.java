package com.jydoc.deliverable4.Service;

import com.jydoc.deliverable4.DTO.LoginDTO;
import com.jydoc.deliverable4.DTO.UserDTO;
import com.jydoc.deliverable4.model.AuthorityModel;
import com.jydoc.deliverable4.model.UserModel;
import com.jydoc.deliverable4.repositories.AuthorityRepository;
import com.jydoc.deliverable4.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service layer for user management operations including:
 * <ul>
 *   <li>User registration and validation</li>
 *   <li>Authentication and credential verification</li>
 *   <li>User role management</li>
 * </ul>
 *
 * <p>All database operations are transactional with appropriate propagation settings.</p>
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);
    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserValidationHelper validationHelper;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;

    // ---------------------- Public API ----------------------

    /**
     * Registers a new user with the system.
     *
     * @param userDto the user data transfer object containing registration details
     * @throws UsernameExistsException if the username is already taken
     * @throws EmailExistsException if the email is already registered
     * @throws IllegalArgumentException if user data is invalid
     */
    @Transactional
    public void registerNewUser(UserDTO userDto) {
        validateRegistration(userDto);
        UserModel user = buildUserFromDto(userDto);
        assignDefaultRole(user);
        userRepository.save(user);
        logger.info("Successfully registered new user: {}", user.getUsername());
    }

    /**
     * Authenticates a user with provided credentials.
     *
     * @param loginDto the login data transfer object containing credentials
     * @return authenticated UserModel
     * @throws AuthenticationException if credentials are invalid
     * @throws IllegalArgumentException if login data is null
     */
    @Transactional(readOnly = true)
    public UserModel authenticate(LoginDTO loginDto) {
        if (loginDto == null) {
            throw new IllegalArgumentException("Login credentials cannot be null");
        }
        return authenticateUser(loginDto.username(), loginDto.password());
    }

    /**
     * Validates login credentials and returns the authenticated user.
     *
     * @param loginDto the login credentials
     * @return authenticated UserModel
     * @throws AuthenticationException if authentication fails
     */
    @Transactional(readOnly = true)
    public UserModel validateLogin(LoginDTO loginDto) {
        String credential = loginDto.username().trim().toLowerCase();
        String rawPassword = loginDto.password();

        logger.debug("Login attempt for: {}", credential);

        UserModel user = userRepository.findByUsernameOrEmail(credential)
                .orElseThrow(() -> {
                    logger.warn("Login failed - user not found: {}", credential);
                    return new AuthenticationException("Invalid credentials");
                });

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            logger.warn("Login failed - password mismatch for user: {}", user.getUsername());
            throw new AuthenticationException("Invalid credentials");
        }

        if (!user.isEnabled()) {
            logger.warn("Login failed - account disabled: {}", user.getUsername());
            throw new AuthenticationException("Account is disabled");
        }

        logger.info("Login successful for user: {}", user.getUsername());
        return user;
    }

    // ---------------------- Core Business Logic ----------------------

    /**
     * Constructs a UserModel from registration DTO with proper encoding and normalization.
     *
     * @param userDto the user registration data
     * @return properly configured UserModel
     */
    private UserModel buildUserFromDto(UserDTO userDto) {
        return UserModel.builder()
                .username(userDto.getUsername().trim())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .email(userDto.getEmail().toLowerCase().trim())
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .build();
    }

    /**
     * Assigns the default role to a new user.
     *
     * @param user the user to receive the default role
     */
    @Transactional(propagation = Propagation.MANDATORY)
    protected void assignDefaultRole(UserModel user) {
        authorityRepository.findByAuthority(DEFAULT_ROLE)
                .ifPresentOrElse(
                        user::addAuthority,
                        () -> {
                            AuthorityModel newRole = new AuthorityModel(DEFAULT_ROLE);
                            user.addAuthority(authorityRepository.save(newRole));
                            logger.debug("Created new default role: {}", DEFAULT_ROLE);
                        }
                );
    }

    // ---------------------- Validation Methods ----------------------

    /**
     * Validates user registration data.
     *
     * @param userDto the user registration data
     * @throws IllegalArgumentException if user data is invalid
     */
    private void validateRegistration(UserDTO userDto) {
        if (userDto == null) {
            throw new IllegalArgumentException("User data cannot be null");
        }
        validationHelper.validateUserRegistration(userDto);
    }

    // ---------------------- Utility Methods ----------------------

    /**
     * Finds an active user by username or email.
     *
     * @param usernameOrEmail the user identifier
     * @return Optional containing the user if found and active
     */
    @Transactional(readOnly = true)
    public Optional<UserModel> findActiveUser(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail.trim().toLowerCase())
                .filter(UserModel::isEnabled);
    }

    // ---------------------- Custom Exceptions ----------------------

    /**
     * Exception thrown when authentication fails.
     */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
            logger.error("Authentication failed: {}", message);
        }
    }

    /**
     * Exception thrown when attempting to register an existing username.
     */
    public static class UsernameExistsException extends RuntimeException {
        public UsernameExistsException(String username) {
            super(String.format("Username '%s' already exists", username));
            logger.warn("Registration attempt with existing username: {}", username);
        }
    }

    /**
     * Exception thrown when attempting to register an existing email.
     */
    public static class EmailExistsException extends RuntimeException {
        public EmailExistsException(String email) {
            super(String.format("Email '%s' is already registered", email));
            logger.warn("Registration attempt with existing email: {}", email);
        }
    }
}