package com.jydoc.deliverable4.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;

/**
 * Configures HTTP session management for the application using in-memory storage.
 * <p>
 * Features include:
 * <ul>
 *   <li>In-memory session storage (default)</li>
 *   <li>Configurable session timeout</li>
 *   <li>Secure cookie settings</li>
 *   <li>Optional header-based session ID resolution</li>
 * </ul>
 */
@Configuration
@EnableSpringHttpSession
public class SessionConfig {

    /**
     * Default session timeout in seconds (30 minutes).
     */
    private static final int DEFAULT_SESSION_TIMEOUT = 1800;

    /**
     * Configures session cookie settings with security best practices.
     * @return Customized cookie serializer
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("JSESSIONID");
        serializer.setCookiePath("/");
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(true); // Requires HTTPS
        serializer.setSameSite("Lax"); // CSRF protection
        return serializer;
    }

    /**
     * Optional: Configures header-based session ID resolution (for API clients).
     * Uncomment if you need to support session tokens in headers.
     */
    // @Bean
    // public HttpSessionIdResolver httpSessionIdResolver() {
    //     return HeaderHttpSessionIdResolver.xAuthToken();
    // }
}