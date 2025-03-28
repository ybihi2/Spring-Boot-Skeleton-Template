package com.jydoc.deliverable4.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Controller for handling dashboard-related requests.
 * <p>
 * Provides authenticated users access to the application dashboard with
 * role-specific information and functionality.
 */
@Controller
public class DashboardController {

    /**
     * Displays the application dashboard for authenticated users.
     *
     * @param authentication The Spring Security authentication object
     * @param model The model to populate with user data
     * @return The dashboard view template name
     * @throws ClassCastException if the principal is not a UserDetails instance
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
     */
    private UserDetails getUserDetails(Authentication authentication) {
        return (UserDetails) authentication.getPrincipal();
    }

    /**
     * Populates the model with user-specific information.
     */
    private void populateModelWithUserDetails(Model model, UserDetails userDetails) {
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("authorities", userDetails.getAuthorities());
        model.addAttribute("isAdmin", hasAdminAuthority(userDetails));
    }

    /**
     * Checks if the user has ADMIN authority.
     */
    private boolean hasAdminAuthority(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}