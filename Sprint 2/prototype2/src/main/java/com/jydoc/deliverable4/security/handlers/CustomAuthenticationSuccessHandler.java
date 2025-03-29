package com.jydoc.deliverable4.security.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

/**
 * Custom authentication success handler that determines the redirect target URL
 * based on the user's authorities after successful login.
 *
 * <p>This handler extends Spring Security's {@link SimpleUrlAuthenticationSuccessHandler}
 * to provide role-based redirection logic:</p>
 * <ul>
 *   <li>Administrators are redirected to the admin dashboard</li>
 *   <li>Regular users are redirected to the standard dashboard</li>
 * </ul>
 *
 * <p><strong>Security Considerations:</strong></p>
 * <ul>
 *   <li>Ensures proper redirection based on verified authorities</li>
 *   <li>Handles already-committed responses gracefully</li>
 *   <li>Follows Spring Security's authentication flow</li>
 * </ul>
 */
public class CustomAuthenticationSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    /**
     * Path for admin dashboard redirect
     */
    private static final String ADMIN_DASHBOARD_URL = "/admin/dashboard";

    /**
     * Path for regular user dashboard redirect
     */
    private static final String USER_DASHBOARD_URL = "/dashboard";

    /**
     * Handles successful authentication by redirecting to the appropriate target URL.
     *
     * @param request        the HTTP request
     * @param response       the HTTP response
     * @param authentication the authentication object containing user authorities
     * @throws IOException if a redirect error occurs
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String targetUrl = determineTargetUrl(authentication);

        if (response.isCommitted()) {
            logger.debug("Response already committed - unable to redirect to " + targetUrl);
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Determines the target URL based on the user's authorities.
     *
     * <p>The logic checks for the presence of ROLE_ADMIN authority:</p>
     * <ul>
     *   <li>If present: redirects to admin dashboard</li>
     *   <li>Otherwise: redirects to regular dashboard</li>
     * </ul>
     *
     * @param authentication the authentication object containing user authorities
     * @return the appropriate target URL
     */
    protected String determineTargetUrl(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(this::isAdminAuthority);

        return isAdmin ? ADMIN_DASHBOARD_URL : USER_DASHBOARD_URL;
    }

    /**
     * Checks if a given authority represents an admin role.
     *
     * @param authority the granted authority to check
     * @return true if the authority is ROLE_ADMIN, false otherwise
     */
    private boolean isAdminAuthority(GrantedAuthority authority) {
        return authority.getAuthority().equals("ROLE_ADMIN");
    }
}