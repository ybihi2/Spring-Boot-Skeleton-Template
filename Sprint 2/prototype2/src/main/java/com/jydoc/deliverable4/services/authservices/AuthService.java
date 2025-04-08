package com.jydoc.deliverable4.services.authservices;

import com.jydoc.deliverable4.dtos.userdtos.LoginDTO;
import com.jydoc.deliverable4.dtos.userdtos.UserDTO;
import com.jydoc.deliverable4.model.auth.AuthorityModel;
import com.jydoc.deliverable4.model.UserModel;
import com.jydoc.deliverable4.repositories.userrepositories.AuthorityRepository;
import com.jydoc.deliverable4.repositories.userrepositories.UserRepository;
import com.jydoc.deliverable4.services.userservices.UserValidationHelper;
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

/**
 * Service handling all authentication and authorization operations including:
 * - User registration and credential management
 * - Login authentication and validation
 * - Account status checks
 * - Role assignment and management
 *
 * <p>This service integrates with Spring Security's authentication mechanisms
 * while providing additional business logic for user management.</p>
 *
 * <p>All methods perform comprehensive validation and throw appropriate exceptions
 * for error conditions.</p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger logger = LogManager.getLogger(AuthService.class);
    private static final String DEFAULT_ROLE = "ROLE_USER";

    // Dependencies injected via constructor (using Lombok @RequiredArgsConstructor)
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserValidationHelper validationHelper;

    /* ====================== Public API Methods ====================== */

    /**
     * Registers a new user in the system with comprehensive validation.
     *
     * <p>Performs the following operations:</p>
     * <ol>
     *   <li>Validates all required user fields</li>
     *   <li>Checks for duplicate username/email</li>
     *   <li>Encodes the password</li>
     *   <li>Assigns default role (ROLE_USER)</li>
     *   <li>Persists the user entity</li>
     * </ol>
     *
     * @param userDto Data Transfer Object containing user registration information
     * @throws IllegalArgumentException if any required field is missing or invalid
     * @throws UsernameExistsException if the username is already taken
     * @throws EmailExistsException if the email is already registered
     */
    @Transactional
    public void registerNewUser(UserDTO userDto) {
        validationHelper.validateUserRegistration(userDto);
        validateUserDto(userDto);
        checkForExistingCredentials(userDto);

        UserModel user = createUserFromDto(userDto);
        assignDefaultRole(user);
        userRepository.save(user);
        logger.info("Registered new user: {}", user.getUsername());
    }

    /**
     * Authenticates a user using Spring Security's authentication manager.
     *
     * <p>This method:</p>
     * <ul>
     *   <li>Delegates authentication to Spring Security</li>
     *   <li>Handles various authentication failure scenarios</li>
     *   <li>Returns the authenticated user entity on success</li>
     * </ul>
     *
     * @param loginDto Data Transfer Object containing login credentials
     * @return Authenticated UserModel entity
     * @throws IllegalArgumentException if loginDto is null
     * @throws AuthenticationException for authentication failures (bad credentials,
     *         disabled account, locked account, etc.)
     */
    @Transactional(readOnly = true)
    public UserModel authenticate(LoginDTO loginDto) {
        if (loginDto == null) {
            throw new IllegalArgumentException("Login credentials cannot be null");
        }
        return authenticateUser(loginDto.username(), loginDto.password());
    }

    /**
     * Validates user credentials directly against the database.
     *
     * <p>This method provides an alternative to Spring Security authentication
     * with more direct control over the validation process.</p>
     *
     * @param loginDto Data Transfer Object containing login credentials
     * @return Validated UserModel entity
     * @throws IllegalArgumentException if loginDto is null
     * @throws AuthenticationException for invalid credentials or account issues
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

        logger.info("Login successful for: {}", user.getUsername());
        return user;
    }

    /* ====================== Core Business Logic ====================== */

    /**
     * Authenticates a user with Spring Security's authentication manager.
     *
     * @param username The username to authenticate
     * @param password The raw (unencoded) password
     * @return Authenticated UserModel entity
     * @throws AuthenticationException wrapping various Spring Security exceptions
     */
    private UserModel authenticateUser(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            return findAuthenticatedUser(authentication.getName());
        } catch (BadCredentialsException e) {
            handleAuthenticationFailure("Bad credentials for user: {}", username, "Invalid username or password");
        } catch (DisabledException e) {
            handleAuthenticationFailure("Disabled account: {}", username, "Account is disabled");
        } catch (LockedException e) {
            handleAuthenticationFailure("Locked account: {}", username, "Account is locked");
        }
        return null; // Unreachable due to exception handling
    }

    /**
     * Creates a new UserModel entity from registration DTO.
     *
     * @param userDto Source data for user creation
     * @return New UserModel with encoded password and trimmed fields
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
     * Assigns the default role (ROLE_USER) to a new user.
     *
     * <p>If the default role doesn't exist in the database, it will be created.</p>
     *
     * @param user The user to receive the default role
     */
    @Transactional(propagation = Propagation.MANDATORY)
    protected void assignDefaultRole(UserModel user) {
        AuthorityModel authority = authorityRepository.findByAuthority(DEFAULT_ROLE)
                .orElseGet(this::createAndSaveNewAuthority);
        user.addAuthority(authority);
    }

    /* ====================== Validation Helpers ====================== */

    /**
     * Validates that all required fields in UserDTO are present and non-empty.
     *
     * @param userDto The DTO to validate
     * @throws IllegalArgumentException if any required field is missing or empty
     */
    private void validateUserDto(UserDTO userDto) {
        if (userDto == null) throw new IllegalArgumentException("UserDTO cannot be null");
        if (userDto.getUsername() == null || userDto.getUsername().trim().isEmpty())
            throw new IllegalArgumentException("Username cannot be empty");
        if (userDto.getPassword() == null || userDto.getPassword().trim().isEmpty())
            throw new IllegalArgumentException("Password cannot be empty");
        if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty())
            throw new IllegalArgumentException("Email cannot be empty");
        if (userDto.getFirstName() == null || userDto.getFirstName().trim().isEmpty())
            throw new IllegalArgumentException("First name cannot be empty");
        if (userDto.getLastName() == null || userDto.getLastName().trim().isEmpty())
            throw new IllegalArgumentException("Last name cannot be empty");
    }

    /**
     * Checks if username or email already exist in the system.
     *
     * @param userDto The DTO containing credentials to check
     * @throws UsernameExistsException if username is taken
     * @throws EmailExistsException if email is registered
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
     * Validates that the provided raw password matches the user's encoded password.
     *
     * @param user The user to validate
     * @param rawPassword The raw (unencoded) password to check
     * @throws AuthenticationException if passwords don't match
     */
    private void validateUserPassword(UserModel user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            logger.warn("Password mismatch for user: {}", user.getUsername());
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
            logger.warn("Disabled account login attempt: {}", user.getUsername());
            throw new AuthenticationException("Account is disabled");
        }
    }

    /* ====================== Repository Helpers ====================== */

    /**
     * Finds a user by either username or email (case-insensitive).
     *
     * @param credential Username or email to search for
     * @return Found UserModel entity
     * @throws AuthenticationException if no user found
     */
    private UserModel findUserByCredential(String credential) {
        return userRepository.findByUsernameOrEmail(credential)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", credential);
                    return new AuthenticationException("Invalid credentials");
                });
    }

    /**
     * Finds a user by username after successful authentication.
     *
     * @param username The authenticated username
     * @return Found UserModel entity
     * @throws AuthenticationException if user not found (unexpected after auth)
     */
    private UserModel findAuthenticatedUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Authenticated user not found: {}", username);
                    return new AuthenticationException("User account error");
                });
    }

    /**
     * Creates and persists a new authority with the default role.
     *
     * @return The newly created AuthorityModel
     */
    private AuthorityModel createAndSaveNewAuthority() {
        AuthorityModel newRole = new AuthorityModel(DEFAULT_ROLE);
        newRole.setUsers(new HashSet<>());
        return authorityRepository.save(newRole);
    }

    /* ====================== Exception Handling ====================== */

    /**
     * Handles authentication failures consistently with logging and exception throwing.
     *
     * @param logMessage The log message template
     * @param username The username that failed authentication
     * @param exceptionMessage The exception message for clients
     * @throws AuthenticationException always
     */
    private void handleAuthenticationFailure(String logMessage, String username, String exceptionMessage) {
        logger.warn(logMessage, username);
        throw new AuthenticationException(exceptionMessage);
    }

    /* ====================== Custom Exceptions ====================== */

    /**
     * Exception indicating authentication failure.
     */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
            logger.error("Authentication failed: {}", message);
        }
    }

    /**
     * Exception indicating duplicate username during registration.
     */
    public static class UsernameExistsException extends RuntimeException {
        public UsernameExistsException(String username) {
            super(String.format("Username '%s' already exists", username));
            logger.warn("Duplicate username: {}", username);
        }
    }

    /**
     * Exception indicating duplicate email during registration.
     */
    public static class EmailExistsException extends RuntimeException {
        public EmailExistsException(String email) {
            super(String.format("Email '%s' already registered", email));
            logger.warn("Duplicate email: {}", email);
        }
    }
}