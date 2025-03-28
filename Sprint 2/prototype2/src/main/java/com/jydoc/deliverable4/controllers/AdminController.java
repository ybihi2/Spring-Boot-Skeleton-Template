package com.jydoc.deliverable4.controllers;

import com.jydoc.deliverable4.model.UserModel;
import com.jydoc.deliverable4.Service.UserService;
import com.jydoc.deliverable4.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private static final String USERS_REDIRECT = "redirect:/admin/users";
    private static final String USER_ATTR = "user";
    private static final String ERROR_ATTR = "error";
    private static final String MESSAGE_ATTR = "message";

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("userCount", userService.getUserCount());

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userManagement(Model model) {
        List<UserModel> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users/list";
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        UserModel user = userService.getUserById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        model.addAttribute(USER_ATTR, user);
        return "admin/users/edit";
    }

    @PostMapping("/users/update")
    public String updateUser(@Valid @ModelAttribute(USER_ATTR) UserModel user,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/users/edit";
        }
        userService.updateUser(user);
        redirectAttributes.addFlashAttribute(MESSAGE_ATTR, "User updated successfully");
        return USERS_REDIRECT;
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (!userService.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("message", "User deleted successfully");
        return "redirect:/admin/users";
    }

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFound(UserNotFoundException ex, Model model) {
        model.addAttribute(ERROR_ATTR, ex.getMessage());
        return "error/user-not-found";
    }

    @GetMapping("/settings")
    public String systemSettings(Model model) {
        return "admin/settings";
    }

    @GetMapping("/activity-logs")
    public String viewActivityLogs(Model model) {
        return "admin/activity-logs";
    }
}