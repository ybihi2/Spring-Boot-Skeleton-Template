package com.jydoc.deliverable4.config;

import com.jydoc.deliverable4.security.auth.CustomUserDetailsService;
import com.jydoc.deliverable4.security.handlers.CustomAuthenticationSuccessHandler;
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
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Security configuration class that defines the application's security policies.
 * This includes authentication, authorization, CSRF protection, session management,
 * and security headers configuration.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Public endpoints that don't require authentication.
     * These include static resources, login/registration pages, and public APIs.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/",
            "/home",
            "/auth/login",
            "/auth/register",
            "/css/**",
            "/js/**",
            "/images/**",
            "/error",
            "/webjars/**",
            "/api/public/**"
    };

    private final CustomUserDetailsService userDetailsService;

    /**
     * Constructs a new SecurityConfig with the required dependencies.
     *
     * @param userDetailsService the custom user details service for authentication
     */
    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configures the security filter chain that defines all security policies.
     *
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        configureAuthorization(http);
        configureFormLogin(http);
        configureLogout(http);
        configureSessionManagement(http);
        configureExceptionHandling(http);
        configureCsrfProtection(http);
        configureSecurityHeaders(http);

        return http.build();
    }

    /**
     * Configures authorization rules for different endpoints.
     *
     * @param http the HttpSecurity object to configure
     * @throws Exception if an error occurs during configuration
     */
    private void configureAuthorization(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        );
    }

    /**
     * Configures form-based login authentication.
     *
     * @param http the HttpSecurity object to configure
     * @throws Exception if an error occurs during configuration
     */
    private void configureFormLogin(HttpSecurity http) throws Exception {
        http.formLogin(form -> form
                .loginPage("/auth/login")  // Custom login page URL
                .loginProcessingUrl("/auth/login")  // URL to submit username/password
                .successHandler(authenticationSuccessHandler())  // Custom success handler
                .failureUrl("/auth/login?error=true")  // Redirect on failure
                .defaultSuccessUrl("/user/dashboard")  // Default success URL
                .usernameParameter("username")  // Username parameter name
                .passwordParameter("password")  // Password parameter name
                .permitAll()  // Allow access to login for everyone
        );
    }

    /**
     * Configures logout behavior.
     *
     * @param http the HttpSecurity object to configure
     * @throws Exception if an error occurs during configuration
     */
    private void configureLogout(HttpSecurity http) throws Exception {
        http.logout(logout -> logout
                .logoutUrl("/auth/logout")  // URL to trigger logout
                .logoutSuccessUrl("/auth/login?logout")  // Redirect after logout
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")  // Cookies to delete
                .invalidateHttpSession(true)  // Invalidate session
                .clearAuthentication(true)  // Clear authentication
                .permitAll()  // Allow access to logout for everyone
        );
    }

    /**
     * Configures session management policies.
     *
     * @param http the HttpSecurity object to configure
     * @throws Exception if an error occurs during configuration
     */
    private void configureSessionManagement(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)  // Create session when needed
                .invalidSessionUrl("/auth/login?invalid-session")  // Redirect for invalid sessions
                .maximumSessions(1)  // Allow only one session per user
                .maxSessionsPreventsLogin(false)  // Don't prevent new logins
                .expiredUrl("/auth/login?session-expired")  // Redirect for expired sessions
        );
    }

    /**
     * Configures exception handling for access denied scenarios.
     *
     * @param http the HttpSecurity object to configure
     * @throws Exception if an error occurs during configuration
     */
    private void configureExceptionHandling(HttpSecurity http) throws Exception {
        http.exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/access-denied")  // Custom access denied page
        );
    }

    /**
     * Configures CSRF protection with exceptions for certain APIs.
     *
     * @param http the HttpSecurity object to configure
     * @throws Exception if an error occurs during configuration
     */
    private void configureCsrfProtection(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")  // Disable CSRF for API endpoints
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())  // Store CSRF token in cookie
        );
    }

    /**
     * Configures various security headers including XSS protection, CSP, and frame options.
     *
     * @param http the HttpSecurity object to configure
     * @throws Exception if an error occurs during configuration
     */
    private void configureSecurityHeaders(HttpSecurity http) throws Exception {
        http.headers(headers -> headers
                .xssProtection(xss -> xss
                        .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)  // Enable XSS protection with block mode
                )
                .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' cdn.jsdelivr.net; " +
                                "style-src 'self' 'unsafe-inline' cdn.jsdelivr.net; img-src 'self' data:")  // Content Security Policy
                )
                .frameOptions(frame -> frame.deny())  // Prevent clickjacking by denying frame embedding
        );
    }

    /**
     * Provides a custom authentication success handler bean.
     *
     * @return the custom authentication success handler
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    /**
     * Provides the authentication manager bean.
     *
     * @param authenticationConfiguration the authentication configuration
     * @return the authentication manager
     * @throws Exception if an error occurs while creating the authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Provides the password encoder bean using BCrypt hashing.
     *
     * @return the password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}