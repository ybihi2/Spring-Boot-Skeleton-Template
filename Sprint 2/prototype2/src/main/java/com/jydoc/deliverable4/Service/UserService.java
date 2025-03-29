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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing user-related operations including registration, authentication,
 * and CRUD operations. Integrates with Spring Security for authentication and authorization.
 *
 * <p>This service handles:</p>
 * <ul>
 *   <li>User registration with comprehensive validation</li>
 *   <li>Multiple authentication methods (Spring Security and direct validation)</li>
 *   <li>Complete user lifecycle management (CRUD operations)</li>
 *   <li>Role assignment and management</li>
 *   <li>Account status tracking (enabled/disabled, locked/unlocked)</li>
 * </ul>
 *
 * <p>Transactions are properly managed with appropriate propagation levels.</p>
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    /**
     * The default role assigned to new users upon registration
     */
    private static final String DEFAULT_ROLE = "ROLE_USER";

    // Injected dependencies
    private final UserValidationHelper validationHelper;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /* ======================== Public API Methods ======================== */

    /**
     * Registers a new user after performing comprehensive validation checks.
     *
     * <p>Validation includes:</p>
     * <ul>
     *   <li>Null checks for all required fields</li>
     *   <li>Empty string validation</li>
     *   <li>Format validation for email</li>
     *   <li>Duplicate username/email checks</li>
     *   <li>Password complexity requirements</li>
     * </ul>
     *
     * @param userDto Data Transfer Object containing user registration information
     * @throws IllegalArgumentException if any validation fails
     * @throws UsernameExistsException if username already exists
     * @throws EmailExistsException if email already registered
     */
    @Transactional
    public void registerNewUser(UserDTO userDto) {
        validationHelper.validateUserRegistration(userDto);
        validateUserDto(userDto);
        checkForExistingCredentials(userDto);

        UserModel user = createUserFromDto(userDto);
        persistUser(user);
    }

    /**
     * Authenticates a user using Spring Security's authentication manager.
     *
     * @param loginDto Contains username and password for authentication
     * @return Authenticated UserModel
     * @throws IllegalArgumentException if loginDto is null
     * @throws AuthenticationException if authentication fails for any reason
     */
    @Transactional(readOnly = true)
    public UserModel authenticate(LoginDTO loginDto) {
        if (loginDto == null) {
            throw new IllegalArgumentException("Login credentials cannot be null");
        }
        return authenticateUser(loginDto.username(), loginDto.password());
    }

    /**
     * Validates user credentials directly against the database (alternative to Spring Security auth).
     *
     * @param loginDto Contains credentials for validation
     * @return Validated UserModel
     * @throws IllegalArgumentException if loginDto is null
     * @throws AuthenticationException if credentials are invalid or account is disabled
     */
    @Transactional(readOnly = true)
    public UserModel validateLogin(LoginDTO loginDto) {
        if (loginDto == null) {
            throw new IllegalArgumentException("Login credentials cannot be null");
        }

        String credential = loginDto.username().trim().toLowerCase();
        String rawPassword = loginDto.password();

        logger.debug("Login attempt for: {}", credential);

        UserModel user = findUserByCredential(credential);
        validateUserPassword(user, rawPassword);
        checkAccountEnabled(user);

        logger.info("Login successful for user: {}", user.getUsername());
        return user;
    }

    /* ======================== User Management Methods ======================== */

    /**
     * Finds an active user by username or email.
     *
     * @param usernameOrEmail The username or email to search for
     * @return Optional containing the user if found and active, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<UserModel> findActiveUser(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail.trim().toLowerCase())
                .filter(UserModel::isEnabled);
    }

    /**
     * Gets the total count of users in the system.
     *
     * @return The number of users in the system
     */
    @Transactional(readOnly = true)
    public long getUserCount() {
        return userRepository.count();
    }

    /**
     * Checks if a user exists with the given ID.
     *
     * @param id The user ID to check
     * @return true if user exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    /**
     * Retrieves all users in the system.
     *
     * @return List of all users
     */
    @Transactional(readOnly = true)
    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Finds a user by their ID.
     *
     * @param id The user ID to find
     * @return Optional containing the user if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<UserModel> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Updates an existing user.
     *
     * @param user The user to update
     */
    @Transactional
    public void updateUser(UserModel user) {
        userRepository.save(user);
    }

    /**
     * Deletes a user by ID.
     *
     * @param id The ID of the user to delete
     */
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /* ======================== Authentication Methods ======================== */

    /**
     * Authenticates user credentials using Spring Security's authentication mechanisms.
     *
     * @param username The username to authenticate
     * @param password The password to verify
     * @return Authenticated UserModel
     * @throws AuthenticationException wrapping various Spring Security exceptions
     */
    @Transactional(readOnly = true)
    public UserModel authenticateUser(String username, String password) {
        try {
            Authentication authentication = performSpringAuthentication(username, password);
            return findAuthenticatedUser(authentication.getName());
        } catch (BadCredentialsException e) {
            handleAuthenticationFailure("Authentication failed - bad credentials for user: {}", username,
                    "Invalid username or password");
        } catch (DisabledException e) {
            handleAuthenticationFailure("Authentication failed - disabled account: {}", username,
                    "Account is disabled");
        } catch (LockedException e) {
            handleAuthenticationFailure("Authentication failed - locked account: {}", username,
                    "Account is locked");
        }
        return null; // Unreachable due to exception handling
    }

    /* ======================== Private Helper Methods ======================== */

    /**
     * Validates the basic structure of the UserDTO.
     *
     * @param userDto The UserDTO to validate
     * @throws IllegalArgumentException if any required field is null or empty
     */
    private void validateUserDto(UserDTO userDto) {
        if (userDto == null) {
            throw new IllegalArgumentException("UserDTO cannot be null");
        }
        if (userDto.getUsername() == null || userDto.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (userDto.getPassword() == null || userDto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (userDto.getFirstName() == null || userDto.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (userDto.getLastName() == null || userDto.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
    }

    /**
     * Checks for existing username or email in the system.
     *
     * @param userDto The UserDTO containing credentials to check
     * @throws UsernameExistsException if username already exists
     * @throws EmailExistsException if email already registered
     */
    private void checkForExistingCredentials(UserDTO userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new UsernameExistsException(userDto.getUsername());
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new EmailExistsException(userDto.getEmail());
        }
    }

    /**
     * Creates and persists a new user with encoded password and default role.
     *
     * @param user The user to persist
     */
    private void persistUser(UserModel user) {
        assignDefaultRole(user);
        userRepository.save(user);
        logger.info("Successfully registered new user: {}", user.getUsername());
    }

    /**
     * Constructs a UserModel from DTO with proper formatting and password encoding.
     *
     * @param userDto The source UserDTO
     * @return Fully constructed UserModel
     */
    private UserModel createUserFromDto(UserDTO userDto) {
        return UserModel.builder()
                .username(userDto.getUsername().trim())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .email(userDto.getEmail().toLowerCase().trim())
                .firstName(userDto.getFirstName().trim())
                .lastName(userDto.getLastName().trim())
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .build();
    }

    /**
     * Finds user by username or email credential.
     *
     * @param credential The username or email to search for
     * @return Found UserModel
     * @throws AuthenticationException if user not found
     */
    private UserModel findUserByCredential(String credential) {
        return userRepository.findByUsernameOrEmail(credential)
                .orElseThrow(() -> {
                    logger.warn("Login failed - user not found: {}", credential);
                    return new AuthenticationException("Invalid credentials");
                });
    }

    /**
     * Validates that the provided password matches the user's stored password.
     *
     * @param user The user to validate
     * @param rawPassword The password to check
     * @throws AuthenticationException if passwords don't match
     */
    private void validateUserPassword(UserModel user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            logger.warn("Login failed - password mismatch for user: {}", user.getUsername());
            throw new AuthenticationException("Invalid credentials");
        }
    }

    /**
     * Verifies that the user account is enabled.
     *
     * @param user The user to check
     * @throws AuthenticationException if account is disabled
     */
    private void checkAccountEnabled(UserModel user) {
        if (!user.isEnabled()) {
            logger.warn("Login failed - account disabled: {}", user.getUsername());
            throw new AuthenticationException("Account is disabled");
        }
    }

    /**
     * Performs Spring Security authentication.
     *
     * @param username The username to authenticate
     * @param password The password to verify
     * @return Authentication object
     */
    private Authentication performSpringAuthentication(String username, String password) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
    }

    /**
     * Retrieves user after successful Spring authentication.
     *
     * @param username The username to find
     * @return Found UserModel
     * @throws AuthenticationException if user not found (shouldn't happen after successful auth)
     */
    private UserModel findAuthenticatedUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Authentication succeeded but user not found: {}", username);
                    return new AuthenticationException("User account error");
                });
    }

    /**
     * Handles authentication failures consistently.
     *
     * @param logMessage The log message template
     * @param username The username that failed authentication
     * @param exceptionMessage The exception message to throw
     * @throws AuthenticationException always
     */
    private void handleAuthenticationFailure(String logMessage, String username, String exceptionMessage) {
        logger.warn(logMessage, username);
        throw new AuthenticationException(exceptionMessage);
    }

    /**
     * Assigns the default role to a user (must be called within a transaction).
     *
     * @param user The user to assign the role to
     */
    @Transactional(propagation = Propagation.MANDATORY)
    protected void assignDefaultRole(UserModel user) {
        AuthorityModel authority = authorityRepository.findByAuthority(DEFAULT_ROLE)
                .orElseGet(this::createAndSaveNewAuthority);
        user.addAuthority(authority);
    }

    /**
     * Creates and persists a new authority role.
     *
     * @return The newly created AuthorityModel
     */
    private AuthorityModel createAndSaveNewAuthority() {
        AuthorityModel newRole = new AuthorityModel(DEFAULT_ROLE);
        newRole.setUsers(new HashSet<>());
        return authorityRepository.save(newRole);
    }

    /* ======================== Custom Exceptions ======================== */

    /**
     * Exception thrown when authentication fails for any reason.
     */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
            logger.error("Authentication failed: {}", message);
        }
    }

    /**
     * Exception thrown when attempting to register with an existing username.
     */
    public static class UsernameExistsException extends RuntimeException {
        public UsernameExistsException(String username) {
            super(String.format("Username '%s' already exists", username));
            logger.warn("Registration attempt with existing username: {}", username);
        }
    }

    /**
     * Exception thrown when attempting to register with an existing email.
     */
    public static class EmailExistsException extends RuntimeException {
        public EmailExistsException(String email) {
            super(String.format("Email '%s' is already registered", email));
            logger.warn("Registration attempt with existing email: {}", email);
        }
    }
}