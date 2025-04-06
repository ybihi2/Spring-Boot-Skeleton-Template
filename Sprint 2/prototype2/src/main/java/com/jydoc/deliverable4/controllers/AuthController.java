package com.jydoc.deliverable4.controllers;

import com.jydoc.deliverable4.dto.UserDTO;
import com.jydoc.deliverable4.dto.LoginDTO;
import com.jydoc.deliverable4.model.UserModel;
import com.jydoc.deliverable4.services.AuthService;
import com.jydoc.deliverable4.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final String USER_ATTRIBUTE = "user";
    private static final String REDIRECT_LOGIN = "redirect:/auth/login";
    private static final String LOGIN_VIEW = "auth/login";
    private static final String REGISTER_VIEW = "auth/register";

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /* ==================== REGISTRATION ==================== */

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserDTO());
        }
        return REGISTER_VIEW;
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") UserDTO userDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            logger.debug("Registration validation errors: {}", result.getAllErrors());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", result);
            redirectAttributes.addFlashAttribute("user", userDto);
            return "redirect:/auth/register";
        }

        try {
            authService.registerNewUser(userDto);
            logger.info("User registered successfully: {}", userDto.getUsername());
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return REDIRECT_LOGIN;
        } catch (AuthService.UsernameExistsException | AuthService.EmailExistsException e) {
            logger.error("Registration failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("user", userDto);
            return "redirect:/auth/register";
        } catch (Exception e) {
            logger.error("Unexpected registration error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Registration failed. Please try again.");
            redirectAttributes.addFlashAttribute("user", userDto);
            return "redirect:/auth/register";
        }
    }

    /* ==================== LOGIN ==================== */

    @GetMapping("/login")
    public String showLoginForm(
            Model model,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout) {

        if (!model.containsAttribute("loginData")) {
            model.addAttribute("loginData", new LoginDTO("", ""));
        }

        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("success", "You have been logged out successfully");
        }

        return LOGIN_VIEW;
    }

    @PostMapping("/login")
    public String loginUser(
            @Valid @ModelAttribute("loginData") LoginDTO loginDto,
            BindingResult result,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            logger.debug("Login validation errors");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.loginData", result);
            redirectAttributes.addFlashAttribute("loginData", loginDto);
            return "redirect:/auth/login";
        }

        try {
            UserModel user = authService.validateLogin(loginDto);
            session.setAttribute(USER_ATTRIBUTE, user);
            logger.info("User logged in: {}", user.getUsername());

            return determineRedirectUrl(user);

        } catch (AuthService.AuthenticationException e) {
            logger.error("Login failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("loginData", loginDto);
            return "redirect:/auth/login";
        }
    }

    /* ==================== LOGOUT ==================== */

    @GetMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public String performLogout(HttpSession session) {
        UserModel user = (UserModel) session.getAttribute(USER_ATTRIBUTE);
        if (user != null) {
            logger.info("User logged out: {}", user.getUsername());
            session.invalidate();
        }
        return REDIRECT_LOGIN + "?logout";
    }

    /* ==================== HELPER METHODS ==================== */

    private String determineRedirectUrl(UserModel user) {
        if (isAdmin(user)) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/user/dashboard";
    }

    private boolean isAdmin(UserModel user) {
        return user.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
    }
}