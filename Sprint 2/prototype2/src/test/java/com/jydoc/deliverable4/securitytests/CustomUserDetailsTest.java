package com.jydoc.deliverable4.securitytests;

import com.jydoc.deliverable4.security.auth.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("CustomUserDetails Tests")
@ActiveProfiles("test")
class CustomUserDetailsTest {

    // Test data
    private static final Long USER_ID = 1L;
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "password123";
    private static final Collection<? extends GrantedAuthority> AUTHORITIES =
            Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        void shouldCreateInstanceWithValidParameters() {
            // Arrange
            Long userId = 1L;
            String username = "testUser";
            String password = "securePassword";
            boolean enabled = true;
            boolean accountNonExpired = true;
            boolean accountNonLocked = true;
            boolean credentialsNonExpired = true;
            Collection<SimpleGrantedAuthority> expectedAuthorities =
                    Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));

            // Act
            CustomUserDetails userDetails = new CustomUserDetails(
                    userId,
                    username,
                    password,
                    enabled,
                    accountNonExpired,
                    accountNonLocked,
                    credentialsNonExpired,
                    expectedAuthorities
            );

            // Assert
            assertEquals(userId, userDetails.getUserId());
            assertEquals(username, userDetails.getUsername());
            assertEquals(password, userDetails.getPassword());
            assertTrue(userDetails.isEnabled());
            assertTrue(userDetails.isAccountNonExpired());
            assertTrue(userDetails.isAccountNonLocked());
            assertTrue(userDetails.isCredentialsNonExpired());

            // This is the key assertion that fixes the original error
            assertIterableEquals(expectedAuthorities, userDetails.getAuthorities());
        }

        @Test
        @DisplayName("Should throw NullPointerException for null userId")
        void shouldThrowForNullUserId() {
            assertThrows(NullPointerException.class, () ->
                    new CustomUserDetails(
                            null, USERNAME, PASSWORD,
                            true, true, true, true,
                            AUTHORITIES
                    )
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException for null username")
        void shouldThrowForNullUsername() {
            assertThrows(NullPointerException.class, () ->
                    new CustomUserDetails(
                            USER_ID, null, PASSWORD,
                            true, true, true, true,
                            AUTHORITIES
                    )
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException for null password")
        void shouldThrowForNullPassword() {
            assertThrows(NullPointerException.class, () ->
                    new CustomUserDetails(
                            USER_ID, USERNAME, null,
                            true, true, true, true,
                            AUTHORITIES
                    )
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException for null authorities")
        void shouldThrowForNullAuthorities() {
            assertThrows(NullPointerException.class, () ->
                    new CustomUserDetails(
                            USER_ID, USERNAME, PASSWORD,
                            true, true, true, true,
                            null
                    )
            );
        }
    }

    @Nested
    @DisplayName("Account Status Tests")
    class AccountStatusTests {

        @Test
        @DisplayName("Should reflect disabled account status")
        void shouldReflectDisabledAccount() {
            CustomUserDetails userDetails = new CustomUserDetails(
                    USER_ID, USERNAME, PASSWORD,
                    false, true, true, true,
                    AUTHORITIES
            );

            assertFalse(userDetails.isEnabled());
        }

        @Test
        @DisplayName("Should reflect expired account status")
        void shouldReflectExpiredAccount() {
            CustomUserDetails userDetails = new CustomUserDetails(
                    USER_ID, USERNAME, PASSWORD,
                    true, false, true, true,
                    AUTHORITIES
            );

            assertFalse(userDetails.isAccountNonExpired());
        }

        @Test
        @DisplayName("Should reflect locked account status")
        void shouldReflectLockedAccount() {
            CustomUserDetails userDetails = new CustomUserDetails(
                    USER_ID, USERNAME, PASSWORD,
                    true, true, false, true,
                    AUTHORITIES
            );

            assertFalse(userDetails.isAccountNonLocked());
        }

        @Test
        @DisplayName("Should reflect expired credentials status")
        void shouldReflectExpiredCredentials() {
            CustomUserDetails userDetails = new CustomUserDetails(
                    USER_ID, USERNAME, PASSWORD,
                    true, true, true, false,
                    AUTHORITIES
            );

            assertFalse(userDetails.isCredentialsNonExpired());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when userId and username match")
        void shouldBeEqualWhenUserIdAndUsernameMatch() {
            CustomUserDetails user1 = new CustomUserDetails(
                    USER_ID, USERNAME, PASSWORD,
                    true, true, true, true,
                    AUTHORITIES
            );

            CustomUserDetails user2 = new CustomUserDetails(
                    USER_ID, USERNAME, "differentPassword",
                    false, false, false, false,
                    Collections.emptyList()
            );

            assertEquals(user1, user2);
            assertEquals(user1.hashCode(), user2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when userId differs")
        void shouldNotBeEqualWhenUserIdDiffers() {
            CustomUserDetails user1 = new CustomUserDetails(
                    USER_ID, USERNAME, PASSWORD,
                    true, true, true, true,
                    AUTHORITIES
            );

            CustomUserDetails user2 = new CustomUserDetails(
                    2L, USERNAME, PASSWORD,
                    true, true, true, true,
                    AUTHORITIES
            );

            assertNotEquals(user1, user2);
        }

        @Test
        @DisplayName("Should not be equal when username differs")
        void shouldNotBeEqualWhenUsernameDiffers() {
            CustomUserDetails user1 = new CustomUserDetails(
                    USER_ID, USERNAME, PASSWORD,
                    true, true, true, true,
                    AUTHORITIES
            );

            CustomUserDetails user2 = new CustomUserDetails(
                    USER_ID, "differentuser", PASSWORD,
                    true, true, true, true,
                    AUTHORITIES
            );

            assertNotEquals(user1, user2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            CustomUserDetails userDetails = new CustomUserDetails(
                    USER_ID, USERNAME, PASSWORD,
                    true, true, true, true,
                    AUTHORITIES
            );

            assertNotEquals(null, userDetails);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            CustomUserDetails userDetails = new CustomUserDetails(
                    USER_ID, USERNAME, PASSWORD,
                    true, true, true, true,
                    AUTHORITIES
            );

            assertNotEquals("Not a user object", userDetails);
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Should return correct userId")
        void shouldReturnCorrectUserId() {
            CustomUserDetails userDetails = new CustomUserDetails(
                    USER_ID, USERNAME, PASSWORD,
                    true, true, true, true,
                    AUTHORITIES
            );

            assertEquals(USER_ID, userDetails.getUserId());
        }

        @Test
        @DisplayName("Should return correct username")
        void shouldReturnCorrectUsername() {
            CustomUserDetails userDetails = new CustomUserDetails(
                    USER_ID, USERNAME, PASSWORD,
                    true, true, true, true,
                    AUTHORITIES
            );

            assertEquals(USERNAME, userDetails.getUsername());
        }

        @Test
        @DisplayName("Should return correct password")
        void shouldReturnCorrectPassword() {
            CustomUserDetails userDetails = new CustomUserDetails(
                    USER_ID, USERNAME, PASSWORD,
                    true, true, true, true,
                    AUTHORITIES
            );

            assertEquals(PASSWORD, userDetails.getPassword());
        }

        @Test
        @DisplayName("Should return correct authorities")
        void shouldReturnCorrectAuthorities() {
            // Setup
            CustomUserDetails userDetails = new CustomUserDetails(
                    USER_ID, USERNAME, PASSWORD,
                    true, true, true, true,
                    AUTHORITIES
            );

            // Verify
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            assertEquals(1, authorities.size());
            GrantedAuthority authority = authorities.iterator().next();
            assertEquals("ROLE_USER", authority.getAuthority());
        }
    }
}