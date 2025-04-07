package com.jydoc.deliverable4.securitytests;

import com.jydoc.deliverable4.initializers.AuthorityInitializer;
import com.jydoc.deliverable4.model.auth.AuthorityModel;
import com.jydoc.deliverable4.repositories.AuthorityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorityInitializer Test Suite")
class AuthorityInitializerTest {

    private static final Set<String> EXPECTED_DEFAULT_AUTHORITIES = Set.of(
            "ROLE_USER",
            "ROLE_ADMIN",
            "ROLE_MODERATOR"
    );
    @Mock
    private AuthorityRepository authorityRepository;
    @InjectMocks
    private AuthorityInitializer authorityInitializer;

    @BeforeEach
    void setUp() {
        reset(authorityRepository);
    }

    @Test
    @DisplayName("Should create all default authorities when none exist")
    void shouldCreateAllDefaultAuthoritiesWhenNoneExist() {
        // Arrange
        when(authorityRepository.findByAuthority(anyString()))
                .thenReturn(Optional.empty());

        // Act
        authorityInitializer.initializeDefaultAuthorities();

        // Assert
        verify(authorityRepository, times(3)).save(any(AuthorityModel.class));
    }

    @Test
    @DisplayName("Should skip creation of existing authorities")
    void shouldSkipCreationOfExistingAuthorities() {
        // Arrange
        AuthorityModel existingAdmin = new AuthorityModel();
        existingAdmin.setAuthority("ROLE_ADMIN");

        when(authorityRepository.findByAuthority("ROLE_ADMIN"))
                .thenReturn(Optional.of(existingAdmin));
        when(authorityRepository.findByAuthority("ROLE_USER"))
                .thenReturn(Optional.empty());
        when(authorityRepository.findByAuthority("ROLE_MODERATOR"))
                .thenReturn(Optional.empty());

        // Act
        authorityInitializer.initializeDefaultAuthorities();

        // Assert
        verify(authorityRepository, times(2)).save(any(AuthorityModel.class));
        verify(authorityRepository, never()).save(existingAdmin);
    }

    @Test
    @DisplayName("Should handle repository exceptions gracefully")
    void shouldHandleRepositoryExceptions() {
        // Arrange
        when(authorityRepository.findByAuthority(anyString()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class,
                () -> authorityInitializer.initializeDefaultAuthorities());

        assertEquals("Database connection failed", exception.getMessage());
    }

    @Test
    @DisplayName("Should verify exact default authority names")
    void shouldVerifyExactDefaultAuthorityNames() {
        // Arrange
        when(authorityRepository.findByAuthority(anyString()))
                .thenReturn(Optional.empty());

        // Act
        authorityInitializer.initializeDefaultAuthorities();

        // Assert
        ArgumentCaptor<String> authorityCaptor = ArgumentCaptor.forClass(String.class);
        verify(authorityRepository, times(3)).findByAuthority(authorityCaptor.capture());

        assertTrue(authorityCaptor.getAllValues().containsAll(EXPECTED_DEFAULT_AUTHORITIES));
    }

    @Test
    @DisplayName("Should not modify existing authorities")
    void shouldNotModifyExistingAuthorities() {
        // Arrange
        AuthorityModel existingUser = new AuthorityModel();
        existingUser.setAuthority("ROLE_USER");

        when(authorityRepository.findByAuthority("ROLE_USER"))
                .thenReturn(Optional.of(existingUser));
        when(authorityRepository.findByAuthority("ROLE_ADMIN"))
                .thenReturn(Optional.empty());
        when(authorityRepository.findByAuthority("ROLE_MODERATOR"))
                .thenReturn(Optional.empty());

        // Act
        authorityInitializer.initializeDefaultAuthorities();

        // Assert
        verify(authorityRepository, never()).save(existingUser);
        verify(authorityRepository, times(2)).save(any(AuthorityModel.class));
    }

    @Test
    @DisplayName("Should handle case when all authorities already exist")
    void shouldHandleCaseWhenAllAuthoritiesExist() {
        // Arrange
        AuthorityModel existingUser = new AuthorityModel();
        existingUser.setAuthority("ROLE_USER");

        AuthorityModel existingAdmin = new AuthorityModel();
        existingAdmin.setAuthority("ROLE_ADMIN");

        AuthorityModel existingModerator = new AuthorityModel();
        existingModerator.setAuthority("ROLE_MODERATOR");

        when(authorityRepository.findByAuthority("ROLE_USER"))
                .thenReturn(Optional.of(existingUser));
        when(authorityRepository.findByAuthority("ROLE_ADMIN"))
                .thenReturn(Optional.of(existingAdmin));
        when(authorityRepository.findByAuthority("ROLE_MODERATOR"))
                .thenReturn(Optional.of(existingModerator));

        // Act
        authorityInitializer.initializeDefaultAuthorities();

        // Assert
        verify(authorityRepository, never()).save(any(AuthorityModel.class));
    }
}