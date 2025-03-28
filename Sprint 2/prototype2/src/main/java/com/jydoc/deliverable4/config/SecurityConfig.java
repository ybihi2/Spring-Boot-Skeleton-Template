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
 * Central security configuration class that defines authentication and authorization rules,
 * CSRF protection, session management, and security headers for the application.
 * <p>
 * This configuration enables:
 * <ul>
 *   <li>Form-based authentication with custom login/logout pages</li>
 *   <li>Role-based authorization (USER and ADMIN)</li>
 *   <li>CSRF protection with cookie-based token storage</li>
 *   <li>JDBC-based session management</li>
 *   <li>BCrypt password hashing</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableJdbcHttpSession
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * Endpoints that are accessible without authentication.
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

    /**
     * Constructs a new SecurityConfig with required dependencies.
     *
     * @param userDetailsService custom implementation of UserDetailsService
     */
    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configures the security filter chain with all security policies.
     *
     * @param http the HttpSecurity to configure
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
     * Configures CSRF protection with exceptions for API endpoints.
     */
    private void configureCsrfProtection(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        );
    }

    /**
     * Configures security-related HTTP headers.
     */
    /**
     * Configures security-related HTTP headers.
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
     * Configures authorization rules for different endpoints.
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
     * Configures form-based authentication with custom login page.
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
     * Configures logout behavior with session invalidation and cookie cleanup.
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
     * Configures session management with concurrency control.
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
     * Configures exception handling for access denied scenarios.
     */
    private void configureExceptionHandling(HttpSecurity http) throws Exception {
        http.exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/access-denied")
        );
    }

    /**
     * Provides the AuthenticationManager bean.
     *
     * @param authenticationConfiguration the authentication configuration
     * @return configured AuthenticationManager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Provides the password encoder bean (BCrypt implementation).
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}