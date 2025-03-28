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

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmailWithAuthorities(usernameOrEmail)
                .map(this::mapToUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username or email: " + usernameOrEmail));
    }

    private UserDetails mapToUserDetails(UserModel user) {
        Set<GrantedAuthority> authorities = user.getAuthorities().stream()
                .map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
                .collect(Collectors.toSet());

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                authorities
        );
    }
}