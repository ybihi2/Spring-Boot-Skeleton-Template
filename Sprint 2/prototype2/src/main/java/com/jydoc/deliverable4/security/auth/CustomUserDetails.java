package com.jydoc.deliverable4.security.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

/**
 * Custom implementation of Spring Security's {@link UserDetails} interface.
 * This class represents an authenticated user's details and is used throughout
 * the security context of the application.
 *
 * <p>It extends Spring Security's core user details with additional user ID field
 * while implementing all required authentication and authorization contract methods.</p>
 *
 * <p>The class is immutable and thread-safe by design, with all fields being final.</p>
 *
 * @author Your Name
 * @version 1.0
 * @see org.springframework.security.core.userdetails.UserDetails
 * @since 1.0
 */
public class CustomUserDetails implements UserDetails {
    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier of the user in the system
     */
    private final Long userId;

    /**
     * The username used to authenticate the user
     */
    private final String username;

    /**
     * The encrypted password of the user
     */
    private final String password;

    /**
     * Flag indicating whether the user is enabled
     */
    private final boolean enabled;

    /**
     * Flag indicating whether the user's account is non-expired
     */
    private final boolean accountNonExpired;

    /**
     * Flag indicating whether the user's account is non-locked
     */
    private final boolean accountNonLocked;

    /**
     * Flag indicating whether the user's credentials are non-expired
     */
    private final boolean credentialsNonExpired;

    /**
     * Collection of authorities (roles/permissions) granted to the user
     */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructs a new CustomUserDetails with the specified parameters.
     *
     * @param userId the unique identifier of the user (cannot be null)
     * @param username the username used for authentication (cannot be null)
     * @param password the encrypted password (cannot be null)
     * @param enabled whether the user is enabled
     * @param accountNonExpired whether the account is non-expired
     * @param accountNonLocked whether the account is non-locked
     * @param credentialsNonExpired whether the credentials are non-expired
     * @param authorities the collection of granted authorities (cannot be null)
     * @throws NullPointerException if any of the non-null parameters are null
     */
    public CustomUserDetails(Long userId, String username, String password,
                             boolean enabled, boolean accountNonExpired,
                             boolean accountNonLocked, boolean credentialsNonExpired,
                             Collection<? extends GrantedAuthority> authorities) {
        this.userId = Objects.requireNonNull(userId);
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.authorities = Objects.requireNonNull(authorities);
    }

    /**
     * Returns the unique identifier of the user.
     *
     * @return the user ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Returns the authorities granted to the user.
     *
     * @return a collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns the password used to authenticate the user.
     *
     * @return the password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used to authenticate the user.
     *
     * @return the username
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates whether the user's account has expired.
     *
     * @return true if the account is non-expired, false otherwise
     */
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    /**
     * Indicates whether the user is locked or unlocked.
     *
     * @return true if the account is non-locked, false otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    /**
     * Indicates whether the user's credentials (password) have expired.
     *
     * @return true if the credentials are non-expired, false otherwise
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     *
     * @return true if the user is enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Compares this CustomUserDetails with another object for equality.
     * Two CustomUserDetails are considered equal if they have the same userId and username.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomUserDetails that = (CustomUserDetails) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(username, that.username);
    }

    /**
     * Returns a hash code value for this CustomUserDetails.
     *
     * @return a hash code value based on userId and username
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }
}