package com.jydoc.deliverable4.controllers;

import com.jydoc.deliverable4.DTO.UserDTO;
import com.jydoc.deliverable4.DTO.LoginDTO;
import com.jydoc.deliverable4.model.UserModel; // Changed from org.apache.catalina.User
import com.jydoc.deliverable4.Service.UserService; // Added service dependency
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    // Constructor injection for UserService
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /* ---------------------- Registration ---------------------- */

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserDTO userDto,
                               BindingResult result) {
        if (result.hasErrors()) {
            return "register";
        }

        // Register the user
        userService.registerNewUser(userDto);
        return "redirect:/login?registered";
    }

    /* ---------------------- Login ---------------------- */

    @GetMapping("/login")
    public String showLoginForm(Model model,
                                @RequestParam(required = false) String error,
                                @RequestParam(required = false) String registered,
                                @RequestParam(required = false) String logout) {


        model.addAttribute("loginData", LoginDTO.empty());

        // Add the parameters as model attributes for Thymeleaf
        model.addAttribute("error", error != null);
        model.addAttribute("registered", registered != null);
        model.addAttribute("logout", logout != null);

        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@Valid @ModelAttribute("loginData") LoginDTO loginDto,
                            BindingResult result,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        logger.debug("Login attempt with data: {}", loginDto);

        if (result.hasErrors()) {
            logger.debug("Validation errors: {}", result.getAllErrors());
            return "login";
        }

        try {
            UserModel user = userService.validateLogin(loginDto);
            session.setAttribute("user", user);
            logger.info("Login successful for: {}", user.getUsername());
            return "redirect:/home";
        } catch (UserService.AuthenticationException e) {
            logger.error("Login failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Invalid credentials");
            return "redirect:/login?error";
        }
    }

    /* ---------------------- Home Page ---------------------- */

    @GetMapping("/home")
    public String showHomePage(Model model, HttpSession session) {
        UserModel user = (UserModel) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("username", user.getUsername());
        }
        return "index";
    }

    /* ---------------------- Logout ---------------------- */

    @GetMapping("/confirm-logout")
    public String showLogoutConfirmation() {
        return "logout";  // Shows the confirmation page
    }

    @PostMapping("/logout")
    public String performLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}