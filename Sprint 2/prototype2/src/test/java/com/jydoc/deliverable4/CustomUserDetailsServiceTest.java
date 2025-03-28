package com.jydoc.deliverable4;

import com.jydoc.deliverable4.model.AuthorityModel;
import com.jydoc.deliverable4.model.UserModel;
import com.jydoc.deliverable4.repositories.UserRepository;
import com.jydoc.deliverable4.security.auth.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private static final String USERNAME = "testuser";
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final Long USER_ID = 1L;

    @Nested
    @DisplayName("loadUserByUsername Tests")
    class LoadUserByUsernameTests {

        private AuthorityModel createMockAuthority(String authorityName) {
            AuthorityModel authority = new AuthorityModel();
            authority.setAuthority(authorityName);
            return authority;
        }

        private UserModel createMockUser(Set<AuthorityModel> authorities) {
            UserModel user = new UserModel();
            user.setId(USER_ID);
            user.setUsername(USERNAME);
            user.setPassword(PASSWORD);
            user.setEnabled(true);
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);
            user.setAuthorities(authorities);
            return user;
        }

        @Test
        @DisplayName("Should load user by username successfully")
        void shouldLoadUserByUsername() {
            // Arrange
            AuthorityModel userRole = createMockAuthority("ROLE_USER");
            AuthorityModel adminRole = createMockAuthority("ROLE_ADMIN");
            Set<AuthorityModel> authorities = Set.of(userRole, adminRole);

            UserModel mockUser = createMockUser(authorities);
            when(userRepository.findByUsernameOrEmailWithAuthorities(USERNAME))
                    .thenReturn(Optional.of(mockUser));

            // Act
            UserDetails userDetails = userDetailsService.loadUserByUsername(USERNAME);

            // Assert
            assertNotNull(userDetails);
            assertEquals(USERNAME, userDetails.getUsername());
            assertEquals(PASSWORD, userDetails.getPassword());
            assertTrue(userDetails.isEnabled());
            assertTrue(userDetails.isAccountNonExpired());
            assertTrue(userDetails.isAccountNonLocked());
            assertTrue(userDetails.isCredentialsNonExpired());

            // Verify authorities
            assertEquals(2, userDetails.getAuthorities().size());
            assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
            assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));

            verify(userRepository).findByUsernameOrEmailWithAuthorities(USERNAME);
        }

        @Test
        @DisplayName("Should load user by email successfully")
        void shouldLoadUserByEmail() {
            // Arrange
            AuthorityModel userRole = createMockAuthority("ROLE_USER");
            UserModel mockUser = createMockUser(Set.of(userRole));
            when(userRepository.findByUsernameOrEmailWithAuthorities(EMAIL))
                    .thenReturn(Optional.of(mockUser));

            // Act
            UserDetails userDetails = userDetailsService.loadUserByUsername(EMAIL);

            // Assert
            assertNotNull(userDetails);
            assertEquals(USERNAME, userDetails.getUsername());
            verify(userRepository).findByUsernameOrEmailWithAuthorities(EMAIL);
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            // Arrange
            String invalidUsername = "nonexistent";
            when(userRepository.findByUsernameOrEmailWithAuthorities(invalidUsername))
                    .thenReturn(Optional.empty());

            // Act & Assert
            UsernameNotFoundException exception = assertThrows(
                    UsernameNotFoundException.class,
                    () -> userDetailsService.loadUserByUsername(invalidUsername)
            );

            assertEquals("User not found with username or email: " + invalidUsername, exception.getMessage());
            verify(userRepository).findByUsernameOrEmailWithAuthorities(invalidUsername);
        }

        @Test
        @DisplayName("Should handle disabled user account")
        void shouldHandleDisabledUser() {
            // Arrange
            UserModel mockUser = createMockUser(Collections.emptySet());
            mockUser.setEnabled(false);
            when(userRepository.findByUsernameOrEmailWithAuthorities(USERNAME))
                    .thenReturn(Optional.of(mockUser));

            // Act
            UserDetails userDetails = userDetailsService.loadUserByUsername(USERNAME);

            // Assert
            assertFalse(userDetails.isEnabled());
        }

        @Test
        @DisplayName("Should handle expired user account")
        void shouldHandleExpiredAccount() {
            // Arrange
            UserModel mockUser = createMockUser(Collections.emptySet());
            mockUser.setAccountNonExpired(false);
            when(userRepository.findByUsernameOrEmailWithAuthorities(USERNAME))
                    .thenReturn(Optional.of(mockUser));

            // Act
            UserDetails userDetails = userDetailsService.loadUserByUsername(USERNAME);

            // Assert
            assertFalse(userDetails.isAccountNonExpired());
        }

        @Test
        @DisplayName("Should handle locked user account")
        void shouldHandleLockedAccount() {
            // Arrange
            UserModel mockUser = createMockUser(Collections.emptySet());
            mockUser.setAccountNonLocked(false);
            when(userRepository.findByUsernameOrEmailWithAuthorities(USERNAME))
                    .thenReturn(Optional.of(mockUser));

            // Act
            UserDetails userDetails = userDetailsService.loadUserByUsername(USERNAME);

            // Assert
            assertFalse(userDetails.isAccountNonLocked());
        }

        @Test
        @DisplayName("Should handle expired credentials")
        void shouldHandleExpiredCredentials() {
            // Arrange
            UserModel mockUser = createMockUser(Collections.emptySet());
            mockUser.setCredentialsNonExpired(false);
            when(userRepository.findByUsernameOrEmailWithAuthorities(USERNAME))
                    .thenReturn(Optional.of(mockUser));

            // Act
            UserDetails userDetails = userDetailsService.loadUserByUsername(USERNAME);

            // Assert
            assertFalse(userDetails.isCredentialsNonExpired());
        }

        @Test
        @DisplayName("Should handle empty authorities")
        void shouldHandleEmptyAuthorities() {
            // Arrange
            UserModel mockUser = createMockUser(Collections.emptySet());
            when(userRepository.findByUsernameOrEmailWithAuthorities(USERNAME))
                    .thenReturn(Optional.of(mockUser));

            // Act
            UserDetails userDetails = userDetailsService.loadUserByUsername(USERNAME);

            // Assert
            assertTrue(userDetails.getAuthorities().isEmpty());
        }
    }

    @Nested
    @DisplayName("Transactional Behavior Tests")
    class TransactionalBehaviorTests {

        @Test
        @DisplayName("Should have readOnly transaction for loadUserByUsername")
        void shouldHaveReadOnlyTransaction() throws NoSuchMethodException {
            // This test verifies the annotation is present
            var method = CustomUserDetailsService.class.getMethod(
                    "loadUserByUsername", String.class);
            var transactional = method.getAnnotation(org.springframework.transaction.annotation.Transactional.class);

            assertNotNull(transactional);
            assertTrue(transactional.readOnly());
        }
    }
}