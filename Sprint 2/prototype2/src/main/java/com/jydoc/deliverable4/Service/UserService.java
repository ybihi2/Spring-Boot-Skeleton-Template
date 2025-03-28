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

import java.util.Optional;

/**
 * Service layer for comprehensive user management including:
 * <ul>
 *   <li>User registration and validation</li>
 *   <li>Authentication and credential verification</li>
 *   <li>User role and authority management</li>
 *   <li>Account status handling</li>
 * </ul>
 *
 * <p>All database operations are transactional with appropriate propagation settings.
 * Integrates with Spring Security for authentication and authorization.</p>
 *
 * <p>This service handles the core business logic for user operations while
 * delegating specific concerns to dedicated components:</p>
 * <ul>
 *   <li>Persistence operations to repositories</li>
 *   <li>Password encoding to PasswordEncoder</li>
 *   <li>Authentication to AuthenticationManager</li>
 *   <li>Validation to UserValidationHelper</li>
 * </ul>
 *
 * @author Your Name
 * @version 1.0
 * @see UserModel
 * @see UserDTO
 * @see LoginDTO
 * @since 1.0
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
    private final AuthenticationManager authenticationManager;

    // ---------------------- Public API ----------------------

    /**
     * Registers a new user with the system after validating all requirements.
     *
     * @param userDto the user data transfer object containing registration details
     * @throws UsernameExistsException if the username is already taken
     * @throws EmailExistsException if the email is already registered
     * @throws IllegalArgumentException if user data is invalid
     * @see #validateRegistration(UserDTO)
     * @see #buildUserFromDto(UserDTO)
     * @see #assignDefaultRole(UserModel)
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
     * Authenticates a user using Spring Security's authentication manager.
     *
     * @param loginDto the login data transfer object containing credentials
     * @return authenticated UserModel entity
     * @throws AuthenticationException if credentials are invalid or account is locked/disabled
     * @throws IllegalArgumentException if login data is null
     * @see #authenticateUser(String, String)
     */
    @Transactional(readOnly = true)
    public UserModel authenticate(LoginDTO loginDto) {
        if (loginDto == null) {
            throw new IllegalArgumentException("Login credentials cannot be null");
        }
        return authenticateUser(loginDto.username(), loginDto.password());
    }

    /**
     * Validates login credentials against stored user data.
     *
     * @param loginDto the login credentials
     * @return authenticated UserModel
     * @throws AuthenticationException if authentication fails
     * @see #findActiveUser(String)
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

    // ---------------------- Authentication Methods ----------------------

    /**
     * Core authentication method using Spring Security's authentication manager.
     *
     * @param username the username to authenticate
     * @param password the raw password to verify
     * @return authenticated UserModel
     * @throws AuthenticationException wrapping various authentication failure scenarios:
     *         <ul>
     *           <li>BadCredentialsException - invalid username/password</li>
     *           <li>DisabledException - account disabled</li>
     *           <li>LockedException - account locked</li>
     *         </ul>
     */
    @Transactional(readOnly = true)
    public UserModel authenticateUser(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            return userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> {
                        logger.error("Authentication succeeded but user not found: {}", username);
                        return new AuthenticationException("User account error");
                    });

        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed - bad credentials for user: {}", username);
            throw new AuthenticationException("Invalid username or password");
        } catch (DisabledException e) {
            logger.warn("Authentication failed - disabled account: {}", username);
            throw new AuthenticationException("Account is disabled");
        } catch (LockedException e) {
            logger.warn("Authentication failed - locked account: {}", username);
            throw new AuthenticationException("Account is locked");
        }
    }

    // ---------------------- Core Business Logic ----------------------

    /**
     * Constructs a UserModel from registration DTO with proper encoding and normalization.
     *
     * @param userDto the user registration data
     * @return properly configured UserModel with:
     *         <ul>
     *           <li>Trimmed username</li>
     *           <li>Encoded password</li>
     *           <li>Normalized email (lowercase and trimmed)</li>
     *           <li>Account flags set to active</li>
     *         </ul>
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
     * Assigns the default role to a new user, creating the role if it doesn't exist.
     *
     * @param user the user to receive the default role
     * @throws IllegalStateException if transaction propagation fails
     * @see AuthorityModel
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
     * @throws UsernameExistsException if username exists
     * @throws EmailExistsException if email exists
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
     * @param usernameOrEmail the user identifier (username or email)
     * @return Optional containing the user if found and active, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<UserModel> findActiveUser(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail.trim().toLowerCase())
                .filter(UserModel::isEnabled);
    }

    // ---------------------- Custom Exceptions ----------------------

    /**
     * Exception thrown when authentication fails for various reasons.
     */
    public static class AuthenticationException extends RuntimeException {
        /**
         * Constructs a new authentication exception with the specified detail message.
         *
         * @param message the detail message
         */
        public AuthenticationException(String message) {
            super(message);
            logger.error("Authentication failed: {}", message);
        }
    }

    /**
     * Exception thrown when attempting to register an existing username.
     */
    public static class UsernameExistsException extends RuntimeException {
        /**
         * Constructs a new username exists exception.
         *
         * @param username the duplicate username
         */
        public UsernameExistsException(String username) {
            super(String.format("Username '%s' already exists", username));
            logger.warn("Registration attempt with existing username: {}", username);
        }
    }

    /**
     * Exception thrown when attempting to register an existing email.
     */
    public static class EmailExistsException extends RuntimeException {
        /**
         * Constructs a new email exists exception.
         *
         * @param email the duplicate email
         */
        public EmailExistsException(String email) {
            super(String.format("Email '%s' is already registered", email));
            logger.warn("Registration attempt with existing email: {}", email);
        }
    }
}