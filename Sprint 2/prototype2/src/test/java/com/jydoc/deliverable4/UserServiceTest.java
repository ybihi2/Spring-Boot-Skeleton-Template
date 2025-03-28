package com.jydoc.deliverable4;

import com.jydoc.deliverable4.DTO.LoginDTO;
import com.jydoc.deliverable4.DTO.UserDTO;
import com.jydoc.deliverable4.Service.UserService;
import com.jydoc.deliverable4.Service.UserValidationHelper;
import com.jydoc.deliverable4.model.AuthorityModel;
import com.jydoc.deliverable4.model.UserModel;
import com.jydoc.deliverable4.repositories.AuthorityRepository;
import com.jydoc.deliverable4.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthorityRepository authorityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserValidationHelper validationHelper;

    @InjectMocks
    private UserService userService;

    private UserDTO testUserDto;
    private LoginDTO testLoginDto;
    private UserModel testUser;
    private AuthorityModel testAuthority;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testUserDto = new UserDTO();
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@example.com");
        testUserDto.setPassword("Password123!");

        testLoginDto = new LoginDTO("testuser", "Password123!");

        testUser = new UserModel();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setEnabled(true);
        testUser.setAuthorities(new HashSet<>());

        testAuthority = AuthorityModel.builder()
                .authority("ROLE_USER")
                .users(new HashSet<>())
                .build();
    }

    // ---------------------- Registration Tests ----------------------

    @Test
    void registerNewUser_ValidUser_Success() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(authorityRepository.findByAuthority("ROLE_USER")).thenReturn(Optional.of(testAuthority));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        // Act
        userService.registerNewUser(testUserDto);

        // Assert
        // Verify validation was called
        verify(validationHelper).validateUserRegistration(testUserDto);

        // Verify repository interactions
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(UserModel.class));

        // Verify authority assignment
        verify(authorityRepository).findByAuthority("ROLE_USER");
    }

    @Test
    void registerNewUser_NullUserDto_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerNewUser(null));
        assertEquals("UserDTO cannot be null", exception.getMessage());
    }

    @Test
    void registerNewUser_ExistingUsername_ThrowsException() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerNewUser(testUserDto));
        assertEquals("Username already exists", exception.getMessage());

        // Verify no user was saved
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    void registerNewUser_ExistingEmail_ThrowsException() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerNewUser(testUserDto));
        assertEquals("Email already registered", exception.getMessage());

        // Verify no attempt to save user
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    void registerNewUser_EmptyPassword_ThrowsException() {
        // Arrange
        testUserDto.setPassword("");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerNewUser(testUserDto));
        assertEquals("Password cannot be empty", exception.getMessage());

        // Verify no repository interactions occurred
        verifyNoInteractions(userRepository);
        verifyNoInteractions(authorityRepository);
    }

    // ... rest of the test methods remain the same ...
}