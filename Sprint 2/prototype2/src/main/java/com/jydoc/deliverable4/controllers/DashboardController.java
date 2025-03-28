package com.jydoc.deliverable4.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashboard(Authentication authentication, Model model) {
        // Get user details from Spring Security
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Add user info to model
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("authorities", userDetails.getAuthorities());
        model.addAttribute("isAdmin", userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));

        return "dashboard";
    }
}