package com.jydoc.deliverable4.controllers;

import com.jydoc.deliverable4.dtos.userdtos.UserDTO;
import com.jydoc.deliverable4.dtos.userdtos.LoginDTO;
import com.jydoc.deliverable4.model.UserModel;
import com.jydoc.deliverable4.services.authservices.AuthService;
import com.jydoc.deliverable4.services.userservices.UserService;
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

/**
 * Controller handling all authentication-related operations including
 * user registration, login, and logout. This controller manages the
 * authentication workflow and redirects users to appropriate views
 * based on their authentication status and roles.
 */
@Controller
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    // Session attribute constants
    private static final String USER_ATTRIBUTE = "user";
    private static final String LOGIN_DATA_ATTRIBUTE = "loginData";
    private static final String BINDING_RESULT_PREFIX = "org.springframework.validation.BindingResult.";

    // View paths
    private static final String REDIRECT_LOGIN = "redirect:/auth/login";
    private static final String LOGIN_VIEW = "auth/login";
    private static final String REGISTER_VIEW = "auth/register";

    // Redirect URLs based on role
    private static final String ADMIN_REDIRECT = "redirect:/admin/dashboard";
    private static final String USER_REDIRECT = "redirect:/user/dashboard";

    // Flash attribute keys
    private static final String ERROR_ATTRIBUTE = "error";
    private static final String SUCCESS_ATTRIBUTE = "success";

    private final AuthService authService;
    private final UserService userService;

    /**
     * Constructs an AuthController with required services.
     *
     * @param authService Service handling authentication logic
     * @param userService Service handling user-related operations
     */
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /* ==================== REGISTRATION ENDPOINTS ==================== */

    /**
     * Displays the user registration form.
     *
     * @param model The Spring MVC model to populate with attributes
     * @return The registration view name
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute(USER_ATTRIBUTE)) {
            model.addAttribute(USER_ATTRIBUTE, new UserDTO());
        }
        return REGISTER_VIEW;
    }

    /**
     * Processes user registration form submission.
     *
     * @param userDto The user data transfer object containing registration details
     * @param result BindingResult for validation errors
     * @param redirectAttributes Attributes for redirect scenarios
     * @return Redirect to appropriate view based on registration outcome
     */
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute(USER_ATTRIBUTE) UserDTO userDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (hasValidationErrors(result, redirectAttributes, userDto)) {
            return "redirect:/auth/register";
        }

        try {
            authService.registerNewUser(userDto);
            logger.info("User registered successfully: {}", userDto.getUsername());
            redirectAttributes.addFlashAttribute(SUCCESS_ATTRIBUTE, "Registration successful! Please login.");
            return REDIRECT_LOGIN;
        } catch (AuthService.UsernameExistsException | AuthService.EmailExistsException e) {
            handleRegistrationError(e.getMessage(), redirectAttributes, userDto);
            return "redirect:/auth/register";
        } catch (Exception e) {
            handleRegistrationError("Registration failed. Please try again.", redirectAttributes, userDto);
            return "redirect:/auth/register";
        }
    }

    /* ==================== LOGIN ENDPOINTS ==================== */

    /**
     * Displays the login form with optional error/success messages.
     *
     * @param model The Spring MVC model to populate with attributes
     * @param error Optional error parameter indicating login failure
     * @param logout Optional logout parameter indicating successful logout
     * @return The login view name
     */
    @GetMapping("/login")
    public String showLoginForm(
            Model model,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout) {

        if (!model.containsAttribute(LOGIN_DATA_ATTRIBUTE)) {
            model.addAttribute(LOGIN_DATA_ATTRIBUTE, new LoginDTO("", ""));
        }

        if (error != null) {
            model.addAttribute(ERROR_ATTRIBUTE, "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute(SUCCESS_ATTRIBUTE, "You have been logged out successfully");
        }

        return LOGIN_VIEW;
    }

    /**
     * Processes login form submission.
     *
     * @param loginDto The login data transfer object containing credentials
     * @param result BindingResult for validation errors
     * @param session The HTTP session to store authentication details
     * @param redirectAttributes Attributes for redirect scenarios
     * @return Redirect to appropriate dashboard based on user role or back to login on failure
     */
    @PostMapping("/login")
    public String loginUser(
            @Valid @ModelAttribute(LOGIN_DATA_ATTRIBUTE) LoginDTO loginDto,
            BindingResult result,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (hasValidationErrors(result, redirectAttributes, loginDto)) {
            return "redirect:/auth/login";
        }

        try {
            UserModel user = authService.validateLogin(loginDto);
            session.setAttribute(USER_ATTRIBUTE, user);
            logger.info("User logged in: {}", user.getUsername());
            return determineRedirectUrl(user);
        } catch (AuthService.AuthenticationException e) {
            handleLoginError(e.getMessage(), redirectAttributes, loginDto);
            return "redirect:/auth/login";
        }
    }

    /* ==================== LOGOUT ENDPOINT ==================== */

    /**
     * Processes user logout by invalidating the session.
     * Requires the user to be authenticated.
     *
     * @param session The HTTP session to invalidate
     * @return Redirect to login page with logout success message
     */
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

    /**
     * Determines the appropriate redirect URL based on user role.
     *
     * @param user The authenticated user model
     * @return Redirect URL string based on user role
     */
    private String determineRedirectUrl(UserModel user) {
        return isAdmin(user) ? ADMIN_REDIRECT : USER_REDIRECT;
    }

    /**
     * Checks if the user has admin privileges.
     *
     * @param user The user model to check
     * @return true if user has ROLE_ADMIN authority, false otherwise
     */
    private boolean isAdmin(UserModel user) {
        return user.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
    }

    /**
     * Handles validation errors by populating redirect attributes.
     *
     * @param result The binding result containing validation errors
     * @param redirectAttributes The redirect attributes to populate
     * @param dto The DTO object that failed validation
     * @return true if validation errors exist, false otherwise
     */
    private boolean hasValidationErrors(BindingResult result,
                                        RedirectAttributes redirectAttributes,
                                        Object dto) {
        if (result.hasErrors()) {
            String attributeName = dto instanceof UserDTO ? USER_ATTRIBUTE : LOGIN_DATA_ATTRIBUTE;
            logger.debug("Validation errors for {}: {}", attributeName, result.getAllErrors());
            redirectAttributes.addFlashAttribute(BINDING_RESULT_PREFIX + attributeName, result);
            redirectAttributes.addFlashAttribute(attributeName, dto);
            return true;
        }
        return false;
    }

    /**
     * Handles registration errors by logging and setting flash attributes.
     *
     * @param errorMessage The error message to display
     * @param redirectAttributes The redirect attributes to populate
     * @param userDto The user DTO associated with the failed registration
     */
    private void handleRegistrationError(String errorMessage,
                                         RedirectAttributes redirectAttributes,
                                         UserDTO userDto) {
        logger.error("Registration failed: {}", errorMessage);
        redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, errorMessage);
        redirectAttributes.addFlashAttribute(USER_ATTRIBUTE, userDto);
    }

    /**
     * Handles login errors by logging and setting flash attributes.
     *
     * @param errorMessage The error message to display
     * @param redirectAttributes The redirect attributes to populate
     * @param loginDto The login DTO associated with the failed attempt
     */
    private void handleLoginError(String errorMessage,
                                  RedirectAttributes redirectAttributes,
                                  LoginDTO loginDto) {
        logger.error("Login failed: {}", errorMessage);
        redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, errorMessage);
        redirectAttributes.addFlashAttribute(LOGIN_DATA_ATTRIBUTE, loginDto);
    }
}