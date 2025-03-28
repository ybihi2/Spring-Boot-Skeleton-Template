package com.jydoc.deliverable4.config;

import com.jydoc.deliverable4.security.auth.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

/**
 * Central security configuration class that defines the application's security policies.
 * <p>
 * This configuration class is responsible for:
 * <ul>
 *   <li>Authentication and authorization rules</li>
 *   <li>CSRF protection configuration</li>
 *   <li>Session management policies</li>
 *   <li>Security headers configuration</li>
 *   <li>Password encoding strategy</li>
 *   <li>Exception handling for security scenarios</li>
 * </ul>
 * </p>
 *
 * <p><strong>Security Features:</strong></p>
 * <ul>
 *   <li>Form-based authentication with custom login/logout pages</li>
 *   <li>Role-based authorization (USER and ADMIN)</li>
 *   <li>CSRF protection with cookie-based token storage (disabled for API endpoints)</li>
 *   <li>JDBC-based session management with concurrency control</li>
 *   <li>BCrypt password hashing with strength 10</li>
 *   <li>Secure HTTP headers configuration (HSTS, frame options)</li>
 * </ul>
 *
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @see org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession
 */
@Configuration
@EnableWebSecurity
@EnableJdbcHttpSession
public class SecurityConfig {

    /**
     * Endpoints that are accessible without authentication.
     * <p>
     * Includes:
     * <ul>
     *   <li>Public pages (home, login, register)</li>
     *   <li>Static resources (CSS, JS, images)</li>
     *   <li>Error pages</li>
     *   <li>Public API endpoints</li>
     *   <li>WebJars resources</li>
     * </ul>
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/",
            "/home",
            "/login",
            "/register",
            "/css/**",
            "/js/**",
            "/images/**",
            "/error",
            "/webjars/**",
            "/api/public/**"
    };

    private final CustomUserDetailsService userDetailsService;

    /**
     * Constructs a new SecurityConfig with required dependencies.
     *
     * @param userDetailsService the custom user details service implementation
     *                           that loads user-specific data. Must not be null.
     */
    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Defines the security filter chain that applies all security configurations.
     * <p>
     * Configures:
     * <ul>
     *   <li>CSRF protection</li>
     *   <li>Security headers</li>
     *   <li>Authorization rules</li>
     *   <li>Form-based authentication</li>
     *   <li>Logout behavior</li>
     *   <li>Session management</li>
     *   <li>Exception handling</li>
     * </ul>
     *
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        configureCsrfProtection(http);
        configureSecurityHeaders(http);
        configureAuthorization(http);
        configureFormBasedAuthentication(http);
        configureLogout(http);
        configureSessionManagement(http);
        configureExceptionHandling(http);

        return http.build();
    }

    /**
     * Configures CSRF protection with cookie-based token storage.
     * <p>
     * Features:
     * <ul>
     *   <li>CSRF protection enabled by default</li>
     *   <li>Disabled for API endpoints (/api/**)</li>
     *   <li>Token stored in a cookie with HttpOnly=false to allow JavaScript access</li>
     * </ul>
     *
     * @param http the HttpSecurity object to configure
     * @throws Exception if configuration fails
     */
    private void configureCsrfProtection(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        );
    }

    /**
     * Configures security-related HTTP headers.
     * <p>
     * Sets:
     * <ul>
     *   <li>Frame options to SAMEORIGIN</li>
     *   <li>HTTP Strict Transport Security (HSTS) with 1-year max age and subdomain inclusion</li>
     * </ul>
     *
     * @param http the HttpSecurity object to configure
     * @throws Exception if configuration fails
     */
    private void configureSecurityHeaders(HttpSecurity http) throws Exception {
        http.headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .maxAgeInSeconds(31536000)
                )
                );
    }

    /**
     * Configures authorization rules for application endpoints.
     * <p>
     * Rules:
     * <ul>
     *   <li>Public endpoints accessible to all</li>
     *   <li>/dashboard requires authentication</li>
     *   <li>/user/** endpoints require ROLE_USER authority</li>
     *   <li>/admin/** endpoints require ROLE_ADMIN authority</li>
     *   <li>All other endpoints require authentication</li>
     * </ul>
     *
     * @param http the HttpSecurity object to configure
     * @throws Exception if configuration fails
     */
    private void configureAuthorization(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers("/dashboard").authenticated()
                .requestMatchers("/user/**", "/api/user/**").hasAuthority("ROLE_USER")
                .requestMatchers("/admin/**", "/api/admin/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
        );
    }

    /**
     * Configures form-based authentication with custom settings.
     * <p>
     * Configuration includes:
     * <ul>
     *   <li>Custom login page at /login</li>
     *   <li>Login processing URL at /perform_login</li>
     *   <li>Default success URL to /dashboard</li>
     *   <li>Failure URL to /login with error parameter</li>
     *   <li>Custom username and password parameter names</li>
     * </ul>
     *
     * @param http the HttpSecurity object to configure
     * @throws Exception if configuration fails
     */
    private void configureFormBasedAuthentication(HttpSecurity http) throws Exception {
        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
        );
    }

    /**
     * Configures logout behavior with security considerations.
     * <p>
     * Features:
     * <ul>
     *   <li>Logout URL at /perform_logout</li>
     *   <li>Redirect to login page with logout parameter</li>
     *   <li>Session invalidation</li>
     *   <li>Cookie cleanup (JSESSIONID, XSRF-TOKEN)</li>
     *   <li>Authentication clearing</li>
     * </ul>
     *
     * @param http the HttpSecurity object to configure
     * @throws Exception if configuration fails
     */
    private void configureLogout(HttpSecurity http) throws Exception {
        http.logout(logout -> logout
                .logoutUrl("/perform_logout")
                .logoutSuccessUrl("/login?logout")
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
        );
    }

    /**
     * Configures session management policies.
     * <p>
     * Settings:
     * <ul>
     *   <li>Session creation policy: IF_REQUIRED</li>
     *   <li>Invalid session redirect URL</li>
     *   <li>Maximum sessions: 1 per user</li>
     *   <li>Session expired redirect URL</li>
     * </ul>
     *
     * @param http the HttpSecurity object to configure
     * @throws Exception if configuration fails
     */
    private void configureSessionManagement(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login?invalid-session")
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/login?session-expired")
        );
    }

    /**
     * Configures exception handling for security scenarios.
     * <p>
     * Redirects to /access-denied when access is denied.
     *
     * @param http the HttpSecurity object to configure
     * @throws Exception if configuration fails
     */
    private void configureExceptionHandling(HttpSecurity http) throws Exception {
        http.exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/access-denied")
        );
    }

    /**
     * Provides the AuthenticationManager bean for authentication processing.
     *
     * @param authenticationConfiguration the authentication configuration
     * @return configured AuthenticationManager
     * @throws Exception if configuration fails
     * @see org.springframework.security.authentication.AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Provides the password encoder bean using BCrypt hashing.
     * <p>
     * Uses BCrypt with strength 10 (2^10 iterations) for password hashing.
     *
     * @return BCryptPasswordEncoder instance
     * @see org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}