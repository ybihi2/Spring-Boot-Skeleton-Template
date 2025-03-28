package com.jydoc.deliverable4.Initializers;

import com.jydoc.deliverable4.model.AuthorityModel;
import com.jydoc.deliverable4.repositories.AuthorityRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorityInitializer {
    private final AuthorityRepository authorityRepository;

    @PostConstruct
    @Transactional
    public void init() {
        // Correct way to check if authority doesn't exist
        if (!authorityRepository.findByAuthority("ROLE_USER").isPresent()) {
            AuthorityModel role = new AuthorityModel();
            role.setAuthority("ROLE_USER");
            authorityRepository.save(role);
        }

        // Alternative cleaner version:
        authorityRepository.findByAuthority("ROLE_USER")
                .orElseGet(() -> {
                    AuthorityModel role = new AuthorityModel();
                    role.setAuthority("ROLE_USER");
                    return authorityRepository.save(role);
                });
    }
}