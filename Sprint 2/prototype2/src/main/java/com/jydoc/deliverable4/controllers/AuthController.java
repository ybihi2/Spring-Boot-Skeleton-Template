package com.jydoc.deliverable4.controllers;

import com.jydoc.deliverable4.DTO.UserDTO;
import com.jydoc.deliverable4.DTO.LoginDTO;
import com.jydoc.deliverable4.model.UserModel;
import com.jydoc.deliverable4.Service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller handling all authentication-related operations including user registration,
 * login/logout functionality, and session management.
 *
 * <p>This controller follows RESTful principles while returning Thymeleaf template names
 * for view resolution. It provides endpoints for:</p>
 * <ul>
 *   <li>User registration flow</li>
 *   <li>Login/logout processes</li>
 *   <li>Session management</li>
 *   <li>Home page display</li>
 * </ul>
 *
 * <p>All endpoints include proper validation and error handling with appropriate
 * redirects and flash attributes.</p>
 */
@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;

    /**
     * Constructs an AuthController with the required UserService dependency.
     *
     * @param userService the service handling user operations and business logic
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /* ==================== REGISTRATION ENDPOINTS ==================== */

    /**
     * Displays the user registration form.
     *
     * @param model the Spring Model to which the empty UserDTO is added
     * @return the "register" template name for view resolution
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserDTO());
        return "register";
    }

    /**
     * Processes user registration form submission.
     *
     * @param userDto the user data transfer object containing registration info
     * @param result  the binding result for validation errors
     * @return redirect to login page on success, or re-show form with errors
     */
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") UserDTO userDto,
            BindingResult result) {

        if (result.hasErrors()) {
            logger.debug("Registration validation errors present: {}", result.getAllErrors());
            return "register";
        }

        try {
            userService.registerNewUser(userDto);
            logger.info("User {} registered successfully", userDto.getUsername());
            return "redirect:/login?registered";
        } catch (Exception e) {
            logger.error("Registration failed for {}: {}", userDto.getUsername(), e.getMessage());
            result.reject("registration.error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    /* ==================== LOGIN ENDPOINTS ==================== */

    /**
     * Displays the login form with optional status indicators.
     *
     * @param model the Spring Model for view attributes
     * @param error optional flag indicating previous login error
     * @param registered optional flag indicating successful registration
     * @param logout optional flag indicating successful logout
     * @return the "login" template name for view resolution
     */
    @GetMapping("/login")
    public String showLoginForm(
            Model model,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String registered,
            @RequestParam(required = false) String logout) {

        model.addAttribute("loginData", LoginDTO.empty());
        model.addAttribute("error", error != null);
        model.addAttribute("registered", registered != null);
        model.addAttribute("logout", logout != null);

        return "login";
    }

    /**
     * Processes login form submission and authenticates the user.
     *
     * @param loginDto the login credentials data transfer object
     * @param result the binding result for validation errors
     * @param session the HTTP session to store authenticated user
     * @param redirectAttributes for flash attributes during redirect
     * @return redirect to appropriate page based on user role, or re-show form with errors
     */
    @PostMapping("/login")
    public String loginUser(
            @Valid @ModelAttribute("loginData") LoginDTO loginDto,
            BindingResult result,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        logger.debug("Processing login for: {}", loginDto.username());

        if (result.hasErrors()) {
            logger.debug("Login validation errors");
            return "login";
        }

        try {
            UserModel user = userService.validateLogin(loginDto);
            session.setAttribute("user", user);

            return isAdmin(user)
                    ? "redirect:/admin/dashboard"
                    : "redirect:/home";

        } catch (UserService.AuthenticationException e) {
            logger.error("Login failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Invalid credentials");
            return "redirect:/login?error";
        }
    }

    /**
     * Checks if the given user has admin privileges.
     *
     * @param user the user model to check
     * @return true if user has admin role, false otherwise
     */
    private boolean isAdmin(UserModel user) {
        return user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    /* ==================== HOME ENDPOINT ==================== */

    /**
     * Displays the home page for authenticated users.
     *
     * @param model the Spring Model for view attributes
     * @param session the HTTP session containing the authenticated user
     * @return the "index" template name for view resolution
     */
    @GetMapping("/home")
    public String showHomePage(Model model, HttpSession session) {
        UserModel user = (UserModel) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("username", user.getUsername());
        }
        return "index";
    }

    /* ==================== LOGOUT ENDPOINTS ==================== */

    /**
     * Displays the logout confirmation page.
     *
     * @return the "logout" template name for view resolution
     */
    @GetMapping("/confirm-logout")
    public String showLogoutConfirmation() {
        return "logout";
    }

    /**
     * Processes logout request by invalidating the session.
     *
     * @param session the HTTP session to invalidate
     * @return redirect to login page with logout flag
     */
    @PostMapping("/logout")
    public String performLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}