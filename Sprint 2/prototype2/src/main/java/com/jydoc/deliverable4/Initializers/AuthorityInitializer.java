package com.jydoc.deliverable4.Initializers;

import com.jydoc.deliverable4.model.AuthorityModel;
import com.jydoc.deliverable4.repositories.AuthorityRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Component responsible for initializing default authorities in the system.
 */
@Component
@RequiredArgsConstructor
public class AuthorityInitializer {

    private static final Set<String> DEFAULT_AUTHORITIES = Set.of(
            "ROLE_USER",
            "ROLE_ADMIN",
            "ROLE_MODERATOR"
    );

    private final AuthorityRepository authorityRepository;

    /**
     * Initializes default authorities if they don't exist.
     */
    @PostConstruct
    @Transactional
    public void initializeDefaultAuthorities() {
        DEFAULT_AUTHORITIES.forEach(this::createAuthorityIfNotExists);
    }

    /**
     * Creates an authority if it doesn't already exist.
     * @param authorityName the name of the authority to create
     */
    public void createAuthorityIfNotExists(String authorityName) {
        authorityRepository.findByAuthority(authorityName)
                .orElseGet(() -> {
                    AuthorityModel authority = new AuthorityModel();
                    authority.setAuthority(authorityName);
                    return authorityRepository.save(authority);
                });
    }
}