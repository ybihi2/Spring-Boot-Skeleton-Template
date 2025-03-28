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
 * Custom implementation of Spring Security's UserDetailsService that loads user-specific data.
 * This service connects the application's user model with Spring Security's authentication framework.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Loading user details by username or email</li>
 *   <li>Mapping application user model to Spring Security's UserDetails</li>
 *   <li>Handling user authority/role conversion</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user details by username or email.
     *
     * @param usernameOrEmail the username or email to search for
     * @return UserDetails containing user information
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        UserModel user = findUserByUsernameOrEmail(usernameOrEmail);
        return createUserDetails(user);
    }

    /**
     * Finds a user by their username or email.
     *
     * @param usernameOrEmail the username or email to search for
     * @return found UserModel
     * @throws UsernameNotFoundException if user is not found
     */
    private UserModel findUserByUsernameOrEmail(String usernameOrEmail) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmailWithAuthorities(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username or email: " + usernameOrEmail));
    }

    /**
     * Creates UserDetails from UserModel.
     *
     * @param user the user model to convert
     * @return UserDetails implementation
     */
    private UserDetails createUserDetails(UserModel user) {
        Set<GrantedAuthority> authorities = mapAuthorities(user);
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                authorities
        );
    }

    /**
     * Maps user authorities to Spring Security GrantedAuthority objects.
     *
     * @param user the user model containing authorities
     * @return set of GrantedAuthority objects
     */
    private Set<GrantedAuthority> mapAuthorities(UserModel user) {
        return user.getAuthorities().stream()
                .map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
                .collect(Collectors.toSet());
    }
}