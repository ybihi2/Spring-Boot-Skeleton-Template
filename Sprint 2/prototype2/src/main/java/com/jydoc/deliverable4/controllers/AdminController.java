package com.jydoc.deliverable4.controllers;

import com.jydoc.deliverable4.model.UserModel;
import com.jydoc.deliverable4.Service.UserService;
import com.jydoc.deliverable4.security.Exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

/**
 * Controller for handling administrative operations.
 * Provides endpoints for user management, system settings, and activity logs.
 * Requires ADMIN role for all operations.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    // Constants for view names and attribute keys
    private static final String USERS_REDIRECT = "redirect:/admin/users";
    private static final String USER_ATTR = "user";
    private static final String ERROR_ATTR = "error";
    private static final String MESSAGE_ATTR = "message";
    private static final String USERS_ATTR = "users";
    private static final String USER_COUNT_ATTR = "userCount";

    // View paths
    private static final String DASHBOARD_VIEW = "admin/dashboard";
    private static final String USER_LIST_VIEW = "admin/users/list";
    private static final String USER_EDIT_VIEW = "admin/users/edit";
    private static final String SETTINGS_VIEW = "admin/settings";
    private static final String ACTIVITY_LOGS_VIEW = "admin/activity-logs";
    private static final String ERROR_VIEW = "error/user-not-found";

    private final UserService userService;

    /**
     * Constructs an AdminController with the required UserService dependency.
     *
     * @param userService The service for user-related operations
     */
    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Displays the admin dashboard with system statistics.
     *
     * @param model The Spring MVC model to add attributes
     * @return The dashboard view name
     */
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute(USER_COUNT_ATTR, userService.getUserCount());
        return DASHBOARD_VIEW;
    }

    /**
     * Displays the user management page with a list of all users.
     *
     * @param model The Spring MVC model to add attributes
     * @return The user list view name
     */
    @GetMapping("/users")
    public String userManagement(Model model) {
        List<UserModel> users = userService.getAllUsers();
        model.addAttribute(USERS_ATTR, users);
        return USER_LIST_VIEW;
    }

    /**
     * Displays the user edit form for a specific user.
     *
     * @param id    The ID of the user to edit
     * @param model The Spring MVC model to add attributes
     * @return The user edit view name
     * @throws UserNotFoundException if the user with the given ID doesn't exist
     */
    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        UserModel user = userService.getUserById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        model.addAttribute(USER_ATTR, user);
        return USER_EDIT_VIEW;
    }

    /**
     * Processes user update requests.
     *
     * @param user               The user data to update (validated)
     * @param result             The binding result for validation errors
     * @param redirectAttributes Attributes for the redirect scenario
     * @return Redirect to user list if successful, back to edit form if validation fails
     */
    @PostMapping("/users/update")
    public String updateUser(@Valid @ModelAttribute(USER_ATTR) UserModel user,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return USER_EDIT_VIEW;
        }
        userService.updateUser(user);
        redirectAttributes.addFlashAttribute(MESSAGE_ATTR, "User updated successfully");
        return USERS_REDIRECT;
    }

    /**
     * Deletes a user with the specified ID.
     *
     * @param id                 The ID of the user to delete
     * @param redirectAttributes Attributes for the redirect scenario
     * @return Redirect to user list
     * @throws UserNotFoundException if the user with the given ID doesn't exist
     */
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (!userService.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute(MESSAGE_ATTR, "User deleted successfully");
        return USERS_REDIRECT;
    }

    /**
     * Handles UserNotFoundException across all controller methods.
     *
     * @param ex    The exception that was thrown
     * @param model The Spring MVC model to add attributes
     * @return The error view name
     */
    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFound(UserNotFoundException ex, Model model) {
        model.addAttribute(ERROR_ATTR, ex.getMessage());
        return ERROR_VIEW;
    }

    /**
     * Displays the system settings page.
     *
     * @param model The Spring MVC model to add attributes
     * @return The settings view name
     */
    @GetMapping("/settings")
    public String systemSettings(Model model) {
        return SETTINGS_VIEW;
    }

    /**
     * Displays the activity logs page.
     *
     * @param model The Spring MVC model to add attributes
     * @return The activity logs view name
     */
    @GetMapping("/activity-logs")
    public String viewActivityLogs(Model model) {
        return ACTIVITY_LOGS_VIEW;
    }
}