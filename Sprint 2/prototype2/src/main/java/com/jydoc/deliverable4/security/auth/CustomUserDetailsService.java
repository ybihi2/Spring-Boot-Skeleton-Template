package com.jydoc.deliverable4.security.auth;

import com.jydoc.deliverable4.model.UserModel;
import com.jydoc.deliverable4.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService}.
 * <p>
 * This service is responsible for loading user-specific data during authentication.
 * It bridges the application's {@link UserModel} with Spring Security's authentication framework.
 *
 * <p>Key responsibilities include:
 * <ul>
 *   <li>Loading user details by username or email</li>
 *   <li>Converting application roles to Spring Security authorities</li>
 *   <li>Handling user not found scenarios</li>
 *   <li>Providing transactional access to user data</li>
 * </ul>
 *
 * @Service Marks this class as a Spring service component
 * @RequiredArgsConstructor Generates constructor for final fields (Dependency Injection)
 * @see UserDetailsService
 * @see UserDetails
 * @see UserModel
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Repository for accessing user data.
     * Injected automatically by Spring via constructor.
     */
    private final UserRepository userRepository;

    /**
     * Loads user details by username or email.
     * <p>
     * This is the core method of the UserDetailsService interface. It:
     * <ol>
     *   <li>Attempts to find the user by username or email</li>
     *   <li>Throws UsernameNotFoundException if user not found</li>
     *   <li>Converts the user's authorities to Spring Security GrantedAuthority objects</li>
     *   <li>Constructs a CustomUserDetails object with all required authentication information</li>
     * </ol>
     *
     * @param usernameOrEmail the username or email address to search for
     * @return UserDetails implementation containing the user's authentication information
     * @throws UsernameNotFoundException if no user is found with the given username/email
     * @implNote The method is transactional with readOnly=true since it only reads data
     * @see CustomUserDetails
     * @see Transactional
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Find user with their authorities in a single query
        UserModel user = userRepository.findByUsernameOrEmailWithAuthorities(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username or email: " + usernameOrEmail));

        // Convert UserModel to Spring Security's UserDetails implementation
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                user.isAccountNonExpired(),
                user.isAccountNonLocked(),
                user.isCredentialsNonExpired(),
                user.getAuthorities().stream()
                        .map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
                        .collect(Collectors.toSet())
        );
    }
}