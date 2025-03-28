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
 * Controller handling authentication-related operations including:
 * - User registration
 * - Login/logout functionality
 * - Session management
 *
 * All endpoints return Thymeleaf template names for view resolution.
 */
@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;

    /**
     * Constructs an AuthController with required UserService dependency
     * @param userService Service handling user operations
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ========== REGISTRATION ENDPOINTS ==========

    /**
     * Displays user registration form
     * @param model Model to add attributes for view
     * @return "register" template name
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserDTO());
        return "register";
    }

    /**
     * Processes user registration form submission
     * @param userDto User data transfer object
     * @param result Validation result
     * @return Redirect to log in on success, re-show form on error
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserDTO userDto,
                               BindingResult result) {
        if (result.hasErrors()) {
            logger.debug("Registration validation errors present");
            return "register";
        }

        userService.registerNewUser(userDto);
        return "redirect:/login?registered";
    }

    // ========== LOGIN ENDPOINTS ==========

    /**
     * Displays login form with optional status indicators
     * @param model Model to add attributes for view
     * @param error Optional error flag from redirect
     * @param registered Optional registration success flag
     * @param logout Optional logout success flag
     * @return "login" template name
     */
    @GetMapping("/login")
    public String showLoginForm(Model model,
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
     * Processes login form submission
     * @param loginDto Login credentials
     * @param result Validation result
     * @param session HTTP session to store user
     * @param redirectAttributes For flash attributes
     * @return Redirect to home on success, re-show form on error
     */
    @PostMapping("/login")
    public String loginUser(@Valid @ModelAttribute("loginData") LoginDTO loginDto,
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
            return "redirect:/home";
        } catch (UserService.AuthenticationException e) {
            logger.error("Login failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Invalid credentials");
            return "redirect:/login?error";
        }
    }

    // ========== HOME ENDPOINT ==========

    /**
     * Displays home page for authenticated users
     * @param model Model to add attributes for view
     * @param session HTTP session containing user
     * @return "index" template name
     */
    @GetMapping("/home")
    public String showHomePage(Model model, HttpSession session) {
        UserModel user = (UserModel) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("username", user.getUsername());
        }
        return "index";
    }

    // ========== LOGOUT ENDPOINTS ==========

    /**
     * Displays logout confirmation page
     * @return "logout" template name
     */
    @GetMapping("/confirm-logout")
    public String showLogoutConfirmation() {
        return "logout";
    }

    /**
     * Processes logout request
     * @param session HTTP session to invalidate
     * @return Redirect to login with logout flag
     */
    @PostMapping("/logout")
    public String performLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}