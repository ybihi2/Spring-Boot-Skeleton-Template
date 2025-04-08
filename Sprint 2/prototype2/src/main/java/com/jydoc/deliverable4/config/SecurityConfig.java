package com.jydoc.deliverable4.config;

import com.jydoc.deliverable4.security.auth.CustomUserDetailsService;
import com.jydoc.deliverable4.security.handlers.CustomAuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configure CSRF token repository
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        tokenRepository.setCookieName("XSRF-TOKEN");
        tokenRepository.setHeaderName("X-XSRF-TOKEN");
        tokenRepository.setCookiePath("/");

        http
                // Configure authorization
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                // Configure form login
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .successHandler(authenticationSuccessHandler())
                        .failureUrl("/auth/login?error=true")
                        .defaultSuccessUrl("/user/dashboard", true)
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                )

                // Configure logout
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout=true")
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .addLogoutHandler(new SecurityContextLogoutHandler())
                        .addLogoutHandler(new CsrfTokenLogoutHandler(tokenRepository))
                        .permitAll()
                )

                // Configure session management
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/auth/login?invalid-session")
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/auth/login?session-expired")
                )

                // Configure CSRF protection
                .csrf(csrf -> csrf
                        .csrfTokenRepository(tokenRepository)
                        .ignoringRequestMatchers("/api/public/**")
                )

                // Configure security headers
                .headers(headers -> headers
                        .xssProtection(xss -> xss
                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                        )
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' cdn.jsdelivr.net; " +
                                        "style-src 'self' 'unsafe-inline' cdn.jsdelivr.net; img-src 'self' data:")
                        )
                        .frameOptions(frame -> frame.deny())
                )

                // Configure exception handling
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/access-denied")
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
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

    /**
     * Custom logout handler to properly clear CSRF tokens
     */
    public static class CsrfTokenLogoutHandler implements LogoutHandler {
        private final CsrfTokenRepository csrfTokenRepository;

        public CsrfTokenLogoutHandler(CsrfTokenRepository csrfTokenRepository) {
            this.csrfTokenRepository = csrfTokenRepository;
        }

        @Override
        public void logout(HttpServletRequest request,
                           HttpServletResponse response,
                           Authentication authentication) {
            // Clear the CSRF token from the repository
            csrfTokenRepository.saveToken(null, request, response);

            // Clear the cookie explicitly
            response.setHeader("Set-Cookie",
                    "XSRF-TOKEN=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
        }
    }
}