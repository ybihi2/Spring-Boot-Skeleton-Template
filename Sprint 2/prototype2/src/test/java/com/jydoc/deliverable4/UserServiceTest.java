package com.jydoc.deliverable4;

import com.jydoc.deliverable4.DTO.LoginDTO;
import com.jydoc.deliverable4.DTO.UserDTO;
import com.jydoc.deliverable4.Service.UserService;
import com.jydoc.deliverable4.Service.UserValidationHelper;
import com.jydoc.deliverable4.model.AuthorityModel;
import com.jydoc.deliverable4.model.UserModel;
import com.jydoc.deliverable4.repositories.AuthorityRepository;
import com.jydoc.deliverable4.repositories.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
        // Initialize UserDTO
        testUserDto = new UserDTO();
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@example.com");
        testUserDto.setPassword("Password123!");

        testLoginDto = new LoginDTO("testuser", "Password123!");

        // Initialize UserModel with proper collections
        testUser = new UserModel();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setEnabled(true);
        testUser.setAccountNonExpired(true);
        testUser.setCredentialsNonExpired(true);
        testUser.setAccountNonLocked(true);
        testUser.setAuthorities(new HashSet<>()); // Initialize authorities collection

        // Initialize AuthorityModel with proper collections
        testAuthority = new AuthorityModel("ROLE_USER");
        testAuthority.setUsers(new HashSet<>()); // Initialize users collection
    }

    // ---------------------- Registration Tests ----------------------

    @Test
    void registerNewUser_ValidUser_Success() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(authorityRepository.findByAuthority("ROLE_USER")).thenReturn(Optional.of(testAuthority));

        // Act
        userService.registerNewUser(testUserDto);

        // Assert
        verify(validationHelper).validateUserRegistration(testUserDto);
        verify(userRepository).save(any(UserModel.class));
        verify(authorityRepository).findByAuthority("ROLE_USER");
    }

    @Test
    void registerNewUser_NullUserDto_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.registerNewUser(null));
    }

    @Test
    void registerNewUser_CreatesDefaultRoleIfNotExists() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(authorityRepository.findByAuthority("ROLE_USER")).thenReturn(Optional.empty());
        when(authorityRepository.save(any(AuthorityModel.class))).thenReturn(testAuthority);

        // Act
        userService.registerNewUser(testUserDto);

        // Assert
        verify(authorityRepository).save(any(AuthorityModel.class));
    }

    // ---------------------- Authentication Tests ----------------------

    @Test
    void authenticate_ValidCredentials_ReturnsUser() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserModel result = userService.authenticate(testLoginDto);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void authenticate_NullLoginDto_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.authenticate(null));
    }

    @Test
    void authenticate_BadCredentials_ThrowsAuthenticationException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(UserService.AuthenticationException.class,
                () -> userService.authenticate(testLoginDto));
    }

    @Test
    void authenticate_DisabledAccount_ThrowsAuthenticationException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("Account disabled"));

        // Act & Assert
        assertThrows(UserService.AuthenticationException.class,
                () -> userService.authenticate(testLoginDto));
    }

    @Test
    void authenticate_LockedAccount_ThrowsAuthenticationException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new LockedException("Account locked"));

        // Act & Assert
        assertThrows(UserService.AuthenticationException.class,
                () -> userService.authenticate(testLoginDto));
    }

    // ---------------------- Validate Login Tests ----------------------

    @Test
    void validateLogin_ValidCredentials_ReturnsUser() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(true);

        // Act
        UserModel result = userService.validateLogin(testLoginDto);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void validateLogin_UserNotFound_ThrowsAuthenticationException() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserService.AuthenticationException.class,
                () -> userService.validateLogin(testLoginDto));
    }

    @Test
    void validateLogin_WrongPassword_ThrowsAuthenticationException() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(UserService.AuthenticationException.class,
                () -> userService.validateLogin(testLoginDto));
    }

    @Test
    void validateLogin_DisabledAccount_ThrowsAuthenticationException() {
        // Arrange
        testUser.setEnabled(false);
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(true);

        // Act & Assert
        assertThrows(UserService.AuthenticationException.class,
                () -> userService.validateLogin(testLoginDto));
    }

    // ---------------------- Find Active User Tests ----------------------

    @Test
    void findActiveUser_ValidUser_ReturnsUser() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<UserModel> result = userService.findActiveUser("testuser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void findActiveUser_DisabledUser_ReturnsEmpty() {
        // Arrange
        testUser.setEnabled(false);
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<UserModel> result = userService.findActiveUser("testuser");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findActiveUser_UserNotFound_ReturnsEmpty() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.empty());

        // Act
        Optional<UserModel> result = userService.findActiveUser("testuser");

        // Assert
        assertTrue(result.isEmpty());
    }

    // ---------------------- Exception Tests ----------------------

    @Test
    void authenticationException_ContainsCorrectMessage() {
        // Act
        UserService.AuthenticationException exception =
                new UserService.AuthenticationException("Test message");

        // Assert
        assertEquals("Test message", exception.getMessage());
    }

    @Test
    void usernameExistsException_ContainsCorrectMessage() {
        // Act
        UserService.UsernameExistsException exception =
                new UserService.UsernameExistsException("testuser");

        // Assert
        assertEquals("Username 'testuser' already exists", exception.getMessage());
    }

    @Test
    void emailExistsException_ContainsCorrectMessage() {
        // Act
        UserService.EmailExistsException exception =
                new UserService.EmailExistsException("test@example.com");

        // Assert
        assertEquals("Email 'test@example.com' is already registered", exception.getMessage());
    }

    // ---------------------- Helper Method Tests ----------------------

    @Test
    void registerNewUser_BuildsCorrectUserModelFromDto() {
        // Arrange
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(authorityRepository.findByAuthority("ROLE_USER")).thenReturn(Optional.of(testAuthority));

        // Use ArgumentCaptor to capture the saved UserModel
        ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);
        when(userRepository.save(userCaptor.capture())).thenReturn(testUser);

        // Act
        userService.registerNewUser(testUserDto);

        // Assert - Verify the built UserModel
        UserModel savedUser = userCaptor.getValue();
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals("test@example.com", savedUser.getEmail());
        assertTrue(savedUser.isEnabled());
        assertTrue(savedUser.isAccountNonExpired());
        assertTrue(savedUser.isCredentialsNonExpired());
        assertTrue(savedUser.isAccountNonLocked());

        // Verify the password was encoded
        verify(passwordEncoder).encode("Password123!");
    }

    @Test
    void registerNewUser_AddsExistingRoleToUser() {
        // Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(authorityRepository.findByAuthority("ROLE_USER")).thenReturn(Optional.of(testAuthority));

        // Use a mock to capture the saved user
        when(userRepository.save(any(UserModel.class))).thenAnswer(invocation -> {
            UserModel savedUser = invocation.getArgument(0);
            // Verify the role was added
            assertTrue(savedUser.getAuthorities().contains(testAuthority));
            return savedUser;
        });

        // Act
        userService.registerNewUser(testUserDto);

        // Assert
        verify(authorityRepository).findByAuthority("ROLE_USER");
        verify(authorityRepository, never()).save(any());
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void registerNewUser_CreatesAndAddsNewRoleToUser() {
        // Arrange
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(authorityRepository.findByAuthority("ROLE_USER")).thenReturn(Optional.empty());

        // Create a new authority with initialized collections
        AuthorityModel newAuthority = new AuthorityModel("ROLE_USER");
        newAuthority.setUsers(new HashSet<>());
        when(authorityRepository.save(any(AuthorityModel.class))).thenReturn(newAuthority);

        // Use ArgumentCaptor to verify the saved user
        ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);
        when(userRepository.save(userCaptor.capture())).thenReturn(testUser);

        // Act
        userService.registerNewUser(testUserDto);

        // Assert
        UserModel savedUser = userCaptor.getValue();
        assertFalse(savedUser.getAuthorities().isEmpty());
        assertEquals("ROLE_USER", savedUser.getAuthorities().iterator().next().getAuthority());
        verify(authorityRepository).findByAuthority("ROLE_USER");
        verify(authorityRepository).save(any(AuthorityModel.class));
    }
}