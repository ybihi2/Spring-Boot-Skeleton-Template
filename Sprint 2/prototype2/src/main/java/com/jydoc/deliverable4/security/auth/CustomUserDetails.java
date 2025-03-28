package com.jydoc.deliverable4.security.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

/**
 * Custom implementation of Spring Security's UserDetails interface.
 * Extends the standard user details with additional user ID field while implementing
 * all required UserDetails contract methods.
 *
 * <p>This class serves as an adapter between the application's user model
 * and Spring Security's authentication framework.
 */
public class CustomUserDetails implements UserDetails {
    private final Long userId;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructs a new CustomUserDetails instance.
     *
     * @param userId the unique user identifier from the application's domain model
     * @param username the username used to authenticate
     * @param password the encrypted password
     * @param enabled flag indicating if the user is enabled
     * @param authorities the user's granted authorities (roles/permissions)
     */
    public CustomUserDetails(Long userId, String username, String password,
                             boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    /**
     * @return the application-specific user ID
     */
    public Long getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    /**
     * @return true if the account is not expired (always true in this implementation)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * @return true if the account is not locked (always true in this implementation)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * @return true if credentials are not expired (always true in this implementation)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Compares users based on username only.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomUserDetails that = (CustomUserDetails) o;
        return Objects.equals(username, that.username);
    }

    /**
     * Generates hash code based on username only.
     */
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    /**
     * Returns a string representation of the user details (excluding password).
     */
    @Override
    public String toString() {
        return "CustomUserDetails{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", enabled=" + enabled +
                ", authorities=" + authorities +
                '}';
    }
}