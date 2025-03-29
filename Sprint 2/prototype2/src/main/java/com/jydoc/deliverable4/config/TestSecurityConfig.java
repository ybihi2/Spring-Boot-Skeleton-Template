package com.jydoc.deliverable4.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test-specific security configuration that disables all security for testing purposes.
 * This configuration is only active when the "test" profile is enabled.
 *
 * <p>Features:
 * <ul>
 *   <li>Disables CSRF protection</li>
 *   <li>Permits all requests without authentication</li>
 *   <li>Disables frame options for easier testing in iframes</li>
 * </ul>
 */
@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class TestSecurityConfig {

    /**
     * Configures a permissive security filter chain for testing environments.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection for testing
                .csrf(csrf -> csrf.disable())

                // Permit all requests without authentication
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )

                // Disable frame options for iframe testing
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                );

        return http.build();
    }
}