package com.jydoc.deliverable4.services;

import com.jydoc.deliverable4.dto.LoginDTO;
import com.jydoc.deliverable4.dto.UserDTO;
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

/**
 * Service handling all authentication and authorization operations including:
 * - User registration
 * - Login authentication
 * - Credential validation
 * - Role assignment
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger logger = LogManager.getLogger(AuthService.class);
    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserValidationHelper validationHelper;

    /* ================ Public Authentication Methods ================ */

    /**
     * Registers a new user with validation and default role assignment
     * @param userDto User registration data
     * @throws IllegalArgumentException if validation fails
     * @throws AuthService.UsernameExistsException if username taken
     * @throws AuthService.EmailExistsException if email registered
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
     * Authenticates using Spring Security's authentication manager
     * @param loginDto Contains username and password
     * @return Authenticated user entity
     * @throws AuthService.AuthenticationException for auth failures
     */
    @Transactional(readOnly = true)
    public UserModel authenticate(LoginDTO loginDto) {
        if (loginDto == null) {
            throw new IllegalArgumentException("Login credentials cannot be null");
        }
        return authenticateUser(loginDto.username(), loginDto.password());
    }

    /**
     * Direct credential validation against database
     * @param loginDto Contains credentials
     * @return Validated user entity
     * @throws AuthService.AuthenticationException for invalid credentials
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

    /* ================ Private Helper Methods ================ */

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

    private void checkForExistingCredentials(UserDTO userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new UsernameExistsException(userDto.getUsername());
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new EmailExistsException(userDto.getEmail());
        }
    }

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

    @Transactional(propagation = Propagation.MANDATORY)
    protected void assignDefaultRole(UserModel user) {
        AuthorityModel authority = authorityRepository.findByAuthority(DEFAULT_ROLE)
                .orElseGet(this::createAndSaveNewAuthority);
        user.addAuthority(authority);
    }

    private AuthorityModel createAndSaveNewAuthority() {
        AuthorityModel newRole = new AuthorityModel(DEFAULT_ROLE);
        newRole.setUsers(new HashSet<>());
        return authorityRepository.save(newRole);
    }

    private UserModel findUserByCredential(String credential) {
        return userRepository.findByUsernameOrEmail(credential)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", credential);
                    return new AuthenticationException("Invalid credentials");
                });
    }

    private void validateUserPassword(UserModel user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            logger.warn("Password mismatch for user: {}", user.getUsername());
            throw new AuthenticationException("Invalid credentials");
        }
    }

    private void checkAccountEnabled(UserModel user) {
        if (!user.isEnabled()) {
            logger.warn("Disabled account login attempt: {}", user.getUsername());
            throw new AuthenticationException("Account is disabled");
        }
    }

    private UserModel findAuthenticatedUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Authenticated user not found: {}", username);
                    return new AuthenticationException("User account error");
                });
    }

    private void handleAuthenticationFailure(String logMessage, String username, String exceptionMessage) {
        logger.warn(logMessage, username);
        throw new AuthenticationException(exceptionMessage);
    }

    /* ================ Custom Exceptions ================ */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
            logger.error("Authentication failed: {}", message);
        }
    }

    public static class UsernameExistsException extends RuntimeException {
        public UsernameExistsException(String username) {
            super(String.format("Username '%s' already exists", username));
            logger.warn("Duplicate username: {}", username);
        }
    }

    public static class EmailExistsException extends RuntimeException {
        public EmailExistsException(String email) {
            super(String.format("Email '%s' already registered", email));
            logger.warn("Duplicate email: {}", email);
        }
    }
}