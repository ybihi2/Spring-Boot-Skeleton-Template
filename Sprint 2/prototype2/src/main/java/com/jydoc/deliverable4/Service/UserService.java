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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);
    private static final String DEFAULT_ROLE = "ROLE_USER";


    private final UserValidationHelper validationHelper;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;

    // Add this temporary test method to your service
    public void verifyPasswordEncoding() {
        String testPassword = "password123";
        String encoded = passwordEncoder.encode(testPassword);
        System.out.println("Encoded password: " + encoded);
        System.out.println("Matches verification: " +
                passwordEncoder.matches(testPassword, encoded));
    }

    // ---------------------- Public API ----------------------

    @Transactional
    public void registerNewUser(UserDTO userDto) {
        validateRegistration(userDto);
        UserModel user = buildUserFromDto(userDto);
        assignDefaultRole(user);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserModel authenticate(LoginDTO loginDto) {
        if (loginDto == null) {
            throw new IllegalArgumentException("Login credentials cannot be null");
        }
        return authenticateUser(loginDto.username(), loginDto.password());
    }

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

        logger.info("Login successful for user: {}", user.getUsername());
        return user;
    }

    // ---------------------- Core Business Logic ----------------------

    private UserModel buildUserFromDto(UserDTO userDto) {
        return UserModel.builder()
                .username(userDto.getUsername().trim())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .email(userDto.getEmail().toLowerCase().trim())
                .enabled(true)
                .build();
    }

    @Transactional
    protected void assignDefaultRole(UserModel user) {
        authorityRepository.findByAuthority("ROLE_USER")
                .ifPresentOrElse(
                        user::addAuthority,
                        () -> {
                            AuthorityModel newRole = new AuthorityModel("ROLE_USER");
                            user.addAuthority(authorityRepository.save(newRole));
                        }
                );
    }

    @Transactional(readOnly = true)
    protected UserModel authenticateUser(String usernameOrEmail, String password) {
        UserModel user = findActiveUser(usernameOrEmail)
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }
        return user;
    }

    // ---------------------- Validation Methods ----------------------

    private void validateRegistration(UserDTO userDto) {

        validationHelper.validateUserRegistration(userDto);
    }

    // ---------------------- Utility Methods ----------------------

    @Transactional(readOnly = true)
    public Optional<UserModel> findActiveUser(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail.trim().toLowerCase())
                .filter(UserModel::isEnabled);
    }

    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username.trim());
    }

    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    // ---------------------- Custom Exceptions ----------------------

    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
            logger.error("Authentication failed: {}", message);
        }
    }

    public static class UsernameExistsException extends RuntimeException {
        public UsernameExistsException(String username) {
            super(String.format("Username '%s' already exists", username));
            logger.warn("Registration attempt with existing username: {}", username);
        }
    }

    public static class EmailExistsException extends RuntimeException {
        public EmailExistsException(String email) {
            super(String.format("Email '%s' is already registered", email));
            logger.warn("Registration attempt with existing email: {}", email);
        }
    }
}