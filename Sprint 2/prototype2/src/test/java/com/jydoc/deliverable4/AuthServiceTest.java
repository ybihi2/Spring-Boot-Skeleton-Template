package com.jydoc.deliverable4;

import com.jydoc.deliverable4.dto.LoginDTO;
import com.jydoc.deliverable4.dto.UserDTO;
import com.jydoc.deliverable4.model.AuthorityModel;
import com.jydoc.deliverable4.model.UserModel;
import com.jydoc.deliverable4.repositories.AuthorityRepository;
import com.jydoc.deliverable4.repositories.UserRepository;
import com.jydoc.deliverable4.services.AuthService;
import com.jydoc.deliverable4.services.UserValidationHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
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
class AuthServiceTest {

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
    private AuthService authService;

    private UserDTO testUserDto;
    private LoginDTO testLoginDto;
    private UserModel testUser;
    private AuthorityModel testAuthority;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDTO();
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@example.com");
        testUserDto.setPassword("Password123!");
        testUserDto.setFirstName("Test");
        testUserDto.setLastName("User");

        testLoginDto = new LoginDTO("testuser", "Password123!");

        testUser = UserModel.builder()
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("encodedPassword")
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .authorities(new HashSet<>())
                .build();

        testAuthority = AuthorityModel.builder()
                .authority("ROLE_USER")
                .users(new HashSet<>())
                .build();
    }

    /* ======================== Registration Tests ======================== */

    @Test
    void registerNewUser_ValidUser_Success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(authorityRepository.findByAuthority("ROLE_USER")).thenReturn(Optional.of(testAuthority));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        authService.registerNewUser(testUserDto);

        verify(validationHelper).validateUserRegistration(testUserDto);
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("Password123!");
        verify(authorityRepository).findByAuthority("ROLE_USER");
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void registerNewUser_NullUserDto_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.registerNewUser(null));
        assertEquals("UserDTO cannot be null", exception.getMessage());
    }

    @Test
    void registerNewUser_ExistingUsername_ThrowsException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        AuthService.UsernameExistsException exception = assertThrows(
                AuthService.UsernameExistsException.class,
                () -> authService.registerNewUser(testUserDto)
        );
        assertEquals("Username 'testuser' already exists", exception.getMessage());
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    void registerNewUser_EmptyPassword_ThrowsException() {
        testUserDto.setPassword("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.registerNewUser(testUserDto));
        assertEquals("Password cannot be empty", exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    /* ======================== Authentication Tests ======================== */

    @Test
    void authenticate_ValidCredentials_ReturnsUser() {
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(auth.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserModel result = authService.authenticate(testLoginDto);

        assertEquals("testuser", result.getUsername());
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("testuser", "Password123!")
        );
    }

    @Test
    void authenticate_BadCredentials_ThrowsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        AuthService.AuthenticationException exception = assertThrows(
                AuthService.AuthenticationException.class,
                () -> authService.authenticate(testLoginDto)
        );
        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void authenticate_DisabledAccount_ThrowsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("Account disabled"));

        AuthService.AuthenticationException exception = assertThrows(
                AuthService.AuthenticationException.class,
                () -> authService.authenticate(testLoginDto)
        );
        assertEquals("Account is disabled", exception.getMessage());
    }

    /* ======================== Direct Validation Tests ======================== */

    @Test
    void validateLogin_ValidCredentials_ReturnsUser() {
        when(userRepository.findByUsernameOrEmail("testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(true);

        UserModel result = authService.validateLogin(testLoginDto);

        assertEquals("testuser", result.getUsername());
        verify(passwordEncoder).matches("Password123!", "encodedPassword");
    }

    @Test
    void validateLogin_InvalidPassword_ThrowsException() {
        when(userRepository.findByUsernameOrEmail("testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(false);

        AuthService.AuthenticationException exception = assertThrows(
                AuthService.AuthenticationException.class,
                () -> authService.validateLogin(testLoginDto)
        );
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void validateLogin_DisabledAccount_ThrowsException() {
        testUser.setEnabled(false);
        when(userRepository.findByUsernameOrEmail("testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(true);

        AuthService.AuthenticationException exception = assertThrows(
                AuthService.AuthenticationException.class,
                () -> authService.validateLogin(testLoginDto)
        );
        assertEquals("Account is disabled", exception.getMessage());
    }

    @Test
    void validateLogin_NullLoginDto_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.validateLogin(null));
        assertEquals("Login credentials cannot be null", exception.getMessage());
    }
}