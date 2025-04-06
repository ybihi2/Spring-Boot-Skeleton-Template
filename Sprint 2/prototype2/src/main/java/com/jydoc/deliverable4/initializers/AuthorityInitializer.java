package com.jydoc.deliverable4.initializers;

import com.jydoc.deliverable4.model.AuthorityModel;
import com.jydoc.deliverable4.repositories.AuthorityRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Component responsible for initializing default system authorities during application startup.
 *
 * <p>This initializer ensures that essential role-based authorities exist in the system
 * before the application becomes fully operational. The initialization occurs automatically
 * after dependency injection is complete.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Transactional initialization to maintain data consistency</li>
 *   <li>Idempotent operations - won't duplicate existing authorities</li>
 *   <li>Predefined set of standard authorities</li>
 *   <li>Lazy creation of missing authorities</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class AuthorityInitializer {

    /**
     * Default system authorities that will be created if they don't exist.
     *
     * <p>Contains the standard role-based authorities used throughout the application:</p>
     * <ul>
     *   <li>ROLE_USER - Basic authenticated user privileges</li>
     *   <li>ROLE_ADMIN - Full administrative privileges</li>
     *   <li>ROLE_MODERATOR - Content moderation privileges</li>
     * </ul>
     */
    private static final Set<String> DEFAULT_AUTHORITIES = Set.of(
            "ROLE_USER",
            "ROLE_ADMIN",
            "ROLE_MODERATOR"
    );

    private final AuthorityRepository authorityRepository;

    /**
     * Initializes default authorities during application startup.
     *
     * <p>This method runs automatically after the bean is constructed and performs:</p>
     * <ol>
     *   <li>Iteration through all default authorities</li>
     *   <li>Conditional creation of each authority if it doesn't exist</li>
     * </ol>
     *
     * <p>The operation is transactional, ensuring all authorities are created atomically.</p>
     */
    @PostConstruct
    @Transactional
    public void initializeDefaultAuthorities() {
        DEFAULT_AUTHORITIES.forEach(this::createAuthorityIfNotExists);
    }

    /**
     * Creates an authority in the system if it doesn't already exist.
     *
     * <p>This method implements an idempotent create operation that:</p>
     * <ol>
     *   <li>Checks for authority existence</li>
     *   <li>Only creates new authority if no matching record exists</li>
     *   <li>Returns the existing authority if found</li>
     * </ol>
     *
     * @param authorityName the name of the authority to create (e.g., "ROLE_ADMIN")
     */
    private void createAuthorityIfNotExists(String authorityName) {
        authorityRepository.findByAuthority(authorityName)
                .orElseGet(() -> {
                    AuthorityModel authority = new AuthorityModel();
                    authority.setAuthority(authorityName);
                    return authorityRepository.save(authority);
                });
    }
}