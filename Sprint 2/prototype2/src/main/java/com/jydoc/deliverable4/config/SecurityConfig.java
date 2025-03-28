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

@Configuration
@EnableWebSecurity
@EnableJdbcHttpSession
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // Public endpoints configuration
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF configuration (enabled for forms, disabled for APIs)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )

                // Security headers
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000) // 1 year
                        )
                )

                // Authorization configuration
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers("/dashboard").authenticated()
                        .requestMatchers("/user/**").hasAuthority("ROLE_USER")
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/user/**").hasAuthority("ROLE_USER")
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()
                )

                // Form login configuration
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                )

                // Logout configuration
                .logout(logout -> logout
                        .logoutUrl("/perform_logout")
                        .logoutSuccessUrl("/login?logout")
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )

                // Session management (stateful)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/login?invalid-session")
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?session-expired")
                )

                // Exception handling
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/access-denied")
                )

                // Set the custom UserDetailsService
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void configureFormLogin(HttpSecurity http) throws Exception {
        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .usernameParameter("usernameOrEmail")  // Update parameter name
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
        );
    }
}