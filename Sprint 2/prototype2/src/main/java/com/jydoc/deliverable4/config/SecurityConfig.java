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
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

/**
 * Security configuration class that defines the application's security policies.
 * This includes authentication, authorization, session management, CSRF protection,
 * and security headers configuration.
 *
 * <p>The configuration enables:
 * <ul>
 *   <li>JDBC-based HTTP session management</li>
 *   <li>Form-based authentication with custom success handler</li>
 *   <li>Role-based authorization</li>
 *   <li>Secure session management</li>
 *   <li>CSRF protection with cookie storage</li>
 *   <li>Security headers including XSS protection and CSP</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableJdbcHttpSession
public class SecurityConfig {

    /**
     * Public endpoints that don't require authentication.
     * These include static resources, public APIs, and authentication-related pages.
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
     * Constructs a new SecurityConfig with the required UserDetailsService.
     *
     * @param userDetailsService the custom user details service for authentication
     */
    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configures the security filter chain that defines all security policies.
     *
     * @param http the HttpSecurity to configure
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
     * Configures authorization rules for endpoints.
     *
     * @param http the HttpSecurity to configure
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
     * Configures form-based login.
     *
     * @param http the HttpSecurity to configure
     * @throws Exception if an error occurs during configuration
     */
    private void configureFormLogin(HttpSecurity http) throws Exception {
        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
        );
    }

    /**
     * Configures logout behavior.
     *
     * @param http the HttpSecurity to configure
     * @throws Exception if an error occurs during configuration
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
     *
     * @param http the HttpSecurity to configure
     * @throws Exception if an error occurs during configuration
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
     * Configures exception handling for security-related exceptions.
     *
     * @param http the HttpSecurity to configure
     * @throws Exception if an error occurs during configuration
     */
    private void configureExceptionHandling(HttpSecurity http) throws Exception {
        http.exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/access-denied")
        );
    }

    /**
     * Configures CSRF protection with exceptions for API endpoints.
     *
     * @param http the HttpSecurity to configure
     * @throws Exception if an error occurs during configuration
     */
    private void configureCsrfProtection(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        );
    }

    /**
     * Configures security headers including XSS protection and Content Security Policy.
     *
     * @param http the HttpSecurity to configure
     * @throws Exception if an error occurs during configuration
     */
    private void configureSecurityHeaders(HttpSecurity http) throws Exception {
        http.headers(headers -> headers
                .xssProtection(xss -> xss
                        .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )
                .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' cdn.jsdelivr.net; " +
                                "style-src 'self' 'unsafe-inline' cdn.jsdelivr.net; img-src 'self' data:")
                )
                .frameOptions(frame -> frame.deny())
        );
    }

    /**
     * Provides a custom authentication success handler bean.
     *
     * @return the configured AuthenticationSuccessHandler
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    /**
     * Provides the AuthenticationManager bean.
     *
     * @param authenticationConfiguration the authentication configuration
     * @return the configured AuthenticationManager
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Provides the password encoder bean using BCrypt hashing.
     *
     * @return the configured PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}