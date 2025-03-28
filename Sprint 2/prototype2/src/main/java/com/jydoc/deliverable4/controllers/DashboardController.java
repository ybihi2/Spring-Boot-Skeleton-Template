package com.jydoc.deliverable4.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Controller for handling dashboard-related requests in the application.
 * <p>
 * This controller manages access to the main application dashboard and ensures
 * that users only see content appropriate for their roles. It provides
 * role-specific views and functionality based on the authenticated user's
 * authorities.
 * </p>
 *
 * <p><strong>Security Note:</strong> All methods in this controller require
 * authenticated access, with role-based checks performed where necessary.</p>
 */
@Controller
public class DashboardController {

    /**
     * Displays the application dashboard for authenticated users.
     * <p>
     * This endpoint:
     * <ul>
     *   <li>Requires the user to be authenticated (via {@code @PreAuthorize})</li>
     *   <li>Retrieves user details from the authentication context</li>
     *   <li>Populates the model with user-specific information</li>
     *   <li>Returns the dashboard view template</li>
     * </ul>
     * </p>
     *
     * @param authentication The Spring Security authentication object containing
     *                       the current user's security context. Must not be null.
     * @param model The Spring MVC model to populate with user data for the view.
     *              Automatically provided by Spring MVC.
     * @return The logical view name "dashboard" which resolves to the dashboard template
     * @throws SecurityException if the authentication principal cannot be cast to
     *         UserDetails, indicating an invalid authentication configuration
     * @see org.springframework.security.core.userdetails.UserDetails
     * @see org.springframework.ui.Model
     */
    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public String showDashboard(Authentication authentication, Model model) {
        try {
            UserDetails userDetails = getUserDetails(authentication);
            populateModelWithUserDetails(model, userDetails);
            return "dashboard";
        } catch (ClassCastException e) {
            throw new SecurityException("Invalid authentication principal type", e);
        }
    }

    /**
     * Extracts UserDetails from the Authentication object.
     *
     * @param authentication The Spring Security authentication object
     * @return The UserDetails object representing the authenticated user
     * @throws ClassCastException if the principal is not a UserDetails instance,
     *         indicating improper authentication configuration
     */
    private UserDetails getUserDetails(Authentication authentication) {
        return (UserDetails) authentication.getPrincipal();
    }

    /**
     * Populates the model with user-specific information for the dashboard view.
     * <p>
     * Adds the following attributes to the model:
     * <ul>
     *   <li>username - The authenticated user's username</li>
     *   <li>authorities - The collection of granted authorities/roles</li>
     *   <li>isAdmin - Boolean flag indicating admin status</li>
     * </ul>
     * </p>
     *
     * @param model The Spring MVC model to populate
     * @param userDetails The UserDetails object containing user information
     */
    private void populateModelWithUserDetails(Model model, UserDetails userDetails) {
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("authorities", userDetails.getAuthorities());
        model.addAttribute("isAdmin", hasAdminAuthority(userDetails));
    }

    /**
     * Determines if the user has ADMIN authority.
     *
     * @param userDetails The UserDetails object to check
     * @return {@code true} if the user has ROLE_ADMIN authority,
     *         {@code false} otherwise
     */
    private boolean hasAdminAuthority(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}