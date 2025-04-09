package com.jydoc.deliverable4.controllers;

import com.jydoc.deliverable4.dtos.userdtos.DashboardDTO;
import com.jydoc.deliverable4.dtos.MedicationDTO;
import com.jydoc.deliverable4.dtos.userdtos.UserDTO;
import com.jydoc.deliverable4.security.Exceptions.PasswordMismatchException;
import com.jydoc.deliverable4.security.Exceptions.WeakPasswordException;
import com.jydoc.deliverable4.services.userservices.DashboardService;
import com.jydoc.deliverable4.services.medicationservices.MedicationService;
import com.jydoc.deliverable4.services.userservices.UserService;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

/**
 * Controller handling all user-related operations including profile management,
 * medication tracking, and dashboard display.
 *
 * <p>This controller serves as the main interface between the user-facing views
 * and backend services for user-specific operations.</p>
 *
 * <p>All endpoints are prefixed with "/user" and require authentication.</p>
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final DashboardService dashboardService;
    private final MedicationService medicationService;
    private final UserService userService;

    /**
     * Constructs a new UserController with required services.
     *
     * @param dashboardService Service for dashboard-related operations
     * @param medicationService Service for medication management
     * @param userService Service for user profile operations
     */
    public UserController(DashboardService dashboardService,
                          MedicationService medicationService,
                          UserService userService) {
        this.dashboardService = dashboardService;
        this.medicationService = medicationService;
        this.userService = userService;
        logger.info("UserController initialized with all required services");
    }

    /**
     * Displays the user dashboard with summary information.
     *
     * @param userDetails Authenticated user details
     * @param model Spring MVC model for view data
     * @return The dashboard view template
     */
    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        logger.debug("Loading dashboard for user: {}", userDetails.getUsername());
        try {
            DashboardDTO dashboard = dashboardService.getUserDashboardData(userDetails);
            boolean hasMedications = dashboardService.hasMedications(userDetails);

            logger.debug("Dashboard data retrieved successfully for user: {}", userDetails.getUsername());
            model.addAttribute("dashboard", dashboard);
            model.addAttribute("hasMedications", hasMedications);

            return "user/dashboard";
        } catch (Exception e) {
            logger.error("Failed to load dashboard for user {}: {}", userDetails.getUsername(), e.getMessage(), e);
            model.addAttribute("error", "Failed to load dashboard data");
            return "user/dashboard";
        }
    }

    /**
     * Displays the user's profile information.
     *
     * @param userDetails Authenticated user details
     * @param model Spring MVC model for view data
     * @return The profile view template
     */
    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        logger.debug("Loading profile for user: {}", userDetails.getUsername());
        try {
            UserDTO user = userService.getUserByUsername(userDetails.getUsername());
            model.addAttribute("user", user);
            logger.info("Profile data loaded successfully for user: {}", userDetails.getUsername());
            return "user/profile";
        } catch (Exception e) {
            logger.error("Failed to load profile for user {}: {}", userDetails.getUsername(), e.getMessage(), e);
            model.addAttribute("error", "Failed to load profile data");
            return "user/profile";
        }
    }

    /**
     * Handles profile updates for the authenticated user.
     *
     * @param userDTO Data transfer object containing updated profile information
     * @param result Binding result for validation errors
     * @param currentPassword Current password for verification
     * @param userDetails Authenticated user details
     * @param redirectAttributes Attributes for redirect scenarios
     * @return Redirect to profile page with success/error message
     */
    @PostMapping("/profile/update")
    public String updateProfile(
            @ModelAttribute("user") UserDTO userDTO,
            BindingResult result,
            @RequestParam("currentPassword") String currentPassword,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        logger.info("Attempting profile update for user: {}", userDetails.getUsername());

        // Manually validate only the fields we want to update
        if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty()) {
            logger.warn("Email validation failed for user: {}", userDetails.getUsername());
            result.rejectValue("email", "NotBlank", "Email is required");
        }
        if (userDTO.getFirstName() == null || userDTO.getFirstName().trim().isEmpty()) {
            logger.warn("First name validation failed for user: {}", userDetails.getUsername());
            result.rejectValue("firstName", "NotBlank", "First name is required");
        }
        if (userDTO.getLastName() == null || userDTO.getLastName().trim().isEmpty()) {
            logger.warn("Last name validation failed for user: {}", userDetails.getUsername());
            result.rejectValue("lastName", "NotBlank", "Last name is required");
        }

        if (result.hasErrors()) {
            logger.warn("Profile update validation failed with {} errors for user: {}",
                    result.getErrorCount(), userDetails.getUsername());
            return "user/profile";
        }

        try {
            logger.debug("Verifying current password for user: {}", userDetails.getUsername());
            if (!userService.verifyCurrentPassword(userDetails.getUsername(), currentPassword)) {
                logger.warn("Current password verification failed for user: {}", userDetails.getUsername());
                redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
                return "redirect:/user/profile";
            }

            // Create a clean DTO with only updatable fields
            UserDTO updateDto = new UserDTO();
            updateDto.setEmail(userDTO.getEmail());
            updateDto.setFirstName(userDTO.getFirstName());
            updateDto.setLastName(userDTO.getLastName());

            logger.debug("Updating profile for user: {}", userDetails.getUsername());
            userService.updateUserProfile(userDetails.getUsername(), updateDto);

            logger.info("Profile updated successfully for user: {}", userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } catch (Exception e) {
            logger.error("Profile update failed for user {}: {}", userDetails.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to update profile");
        }

        return "redirect:/user/profile";
    }

    /**
     * Handles password change requests for authenticated users.
     *
     * @param currentPassword User's current password for verification
     * @param newPassword New password to set
     * @param confirmPassword Confirmation of new password
     * @param userDetails Authenticated user details
     * @param redirectAttributes Attributes for redirect scenarios
     * @return Redirect to profile page with success/error message
     */
    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam("currentPassword") @NotBlank String currentPassword,
            @RequestParam("newPassword") @NotBlank String newPassword,
            @RequestParam("confirmPassword") @NotBlank String confirmPassword,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        logger.info("Password change request received for user: {}", userDetails.getUsername());

        // Validate password match first
        if (!newPassword.equals(confirmPassword)) {
            logger.warn("Password mismatch during change attempt for user: {}", userDetails.getUsername());
            redirectAttributes.addFlashAttribute("error", "New passwords do not match");
            return "redirect:/user/profile";
        }

        try {
            logger.debug("Attempting password change for user: {}", userDetails.getUsername());
            boolean success = userService.changePassword(
                    userDetails.getUsername(),
                    currentPassword,
                    newPassword
            );

            if (!success) {
                logger.warn("Password change failed - current password incorrect for user: {}",
                        userDetails.getUsername());
                throw new PasswordMismatchException("Current password is incorrect");
            }

            logger.info("Password changed successfully for user: {}", userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Password changed successfully");
        } catch (PasswordMismatchException e) {
            logger.warn("Password verification failed for user {}: {}",
                    userDetails.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid password change request for user {}: {}",
                    userDetails.getUsername(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            logger.error("Password change failed unexpectedly for user {}: {}",
                    userDetails.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to change password");
        }
        return "redirect:/user/profile";
    }

    /**
     * Handles account deletion requests.
     *
     * @param password Current password for verification
     * @param confirmDelete User confirmation flag
     * @param userDetails Authenticated user details
     * @param redirectAttributes Attributes for redirect scenarios
     * @return Redirect to logout on success, profile page on failure
     */
    @PostMapping("/profile/delete")
    public String deleteAccount(
            @RequestParam("deletePassword") String password,
            @RequestParam("confirmDelete") boolean confirmDelete,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        logger.info("Account deletion request received for user: {}", userDetails.getUsername());

        if (!confirmDelete) {
            logger.warn("Account deletion not confirmed for user: {}", userDetails.getUsername());
            redirectAttributes.addFlashAttribute("error", "Please confirm account deletion");
            return "redirect:/user/profile";
        }

        try {
            logger.debug("Attempting account deletion for user: {}", userDetails.getUsername());
            boolean deleted = userService.deleteAccount(userDetails.getUsername(), password);

            if (deleted) {
                logger.info("Account deleted successfully for user: {}", userDetails.getUsername());
                return "redirect:/auth/logout";
            } else {
                logger.warn("Incorrect password provided for account deletion by user: {}",
                        userDetails.getUsername());
                redirectAttributes.addFlashAttribute("error", "Incorrect password");
            }
        } catch (Exception e) {
            logger.error("Account deletion failed for user {}: {}",
                    userDetails.getUsername(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete account");
        }
        return "redirect:/user/profile";
    }

    /**
     * Displays the user's medication list.
     *
     * @param userDetails Authenticated user details
     * @param model Spring MVC model for view data
     * @return The medication list view template
     */
    @GetMapping("/medication")
    public String showMedications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        logger.debug("Loading medications for user: {}", userDetails.getUsername());
        try {
            model.addAttribute("medications",
                    medicationService.getUserMedications(userDetails.getUsername()));
            logger.info("Medications loaded successfully for user: {}", userDetails.getUsername());
            return "user/medication/list";
        } catch (Exception e) {
            logger.error("Failed to load medications for user {}: {}",
                    userDetails.getUsername(), e.getMessage(), e);
            model.addAttribute("error", "Failed to load medications");
            return "user/medication/list";
        }
    }

    /**
     * Alternative endpoint for displaying medication list.
     *
     * @param userDetails Authenticated user details
     * @param model Spring MVC model for view data
     * @return The medication list view template
     */
    @GetMapping("/medication/list")
    public String showMedicationList(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        logger.debug("Loading medications via list endpoint for user: {}", userDetails.getUsername());
        try {
            model.addAttribute("medications",
                    medicationService.getUserMedications(userDetails.getUsername()));
            return "user/medication/list";
        } catch (Exception e) {
            logger.error("Failed to load medications via list endpoint for user {}: {}",
                    userDetails.getUsername(), e.getMessage(), e);
            model.addAttribute("error", "Failed to load medications");
            return "user/medication/list";
        }
    }

    /**
     * Displays the form for adding new medications.
     *
     * @param model Spring MVC model for view data
     * @return The add medication form view
     */
    @GetMapping("/medication/add")
    public String showAddMedicationForm(Model model) {
        logger.debug("Displaying add medication form");
        model.addAttribute("medicationDTO", new MedicationDTO());
        return "user/medication/add";
    }

    /**
     * Handles submission of new medication information.
     *
     * @param medicationDTO Data transfer object containing medication details
     * @param result Binding result for validation errors
     * @param userDetails Authenticated user details
     * @return Redirect to medication list on success, form view on failure
     */
    @PostMapping("/medication")
    public String addMedication(
            @Valid @ModelAttribute("medicationDTO") MedicationDTO medicationDTO,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.info("Attempting to add new medication for user: {}", userDetails.getUsername());

        if (result.hasErrors()) {
            logger.warn("Medication validation failed with {} errors for user: {}",
                    result.getErrorCount(), userDetails.getUsername());
            return "user/medication/add";
        }

        try {
            medicationService.createMedication(medicationDTO, userDetails.getUsername());
            logger.info("Medication added successfully for user: {}", userDetails.getUsername());
            return "redirect:/user/medication?success";
        } catch (Exception e) {
            logger.error("Failed to add medication for user {}: {}",
                    userDetails.getUsername(), e.getMessage(), e);
            return "user/medication/add";
        }
    }

    /**
     * Displays the form for editing existing medications.
     *
     * @param id ID of the medication to edit
     * @param model Spring MVC model for view data
     * @return The edit medication form view
     */
    @GetMapping("/medication/{id}/edit")
    public String showEditMedicationForm(@PathVariable Long id, Model model) {
        logger.debug("Displaying edit form for medication ID: {}", id);
        try {
            MedicationDTO medicationDTO = medicationService.getMedicationById(id);
            model.addAttribute("medicationDTO", medicationDTO);
            return "user/medication/edit";
        } catch (Exception e) {
            logger.error("Failed to load medication for editing (ID: {}): {}", id, e.getMessage(), e);
            model.addAttribute("error", "Failed to load medication data");
            return "user/medication/edit";
        }
    }

    /**
     * Handles submission of updated medication information.
     *
     * @param id ID of the medication being updated
     * @param medicationDTO Updated medication details
     * @param result Binding result for validation errors
     * @param model Spring MVC model for view data
     * @return Redirect to medication list on success, form view on failure
     */
    @PostMapping("/medication/{id}")
    public String updateMedication(
            @PathVariable Long id,
            @Valid @ModelAttribute("medicationDTO") MedicationDTO medicationDTO,
            BindingResult result,
            Model model) {

        logger.info("Attempting to update medication ID: {}", id);

        if (result.hasErrors()) {
            logger.warn("Medication update validation failed with {} errors for ID: {}",
                    result.getErrorCount(), id);
            return "user/medication/edit";
        }

        try {
            medicationService.updateMedication(id, medicationDTO);
            logger.info("Medication updated successfully (ID: {})", id);
            return "redirect:/user/medication?updated";
        } catch (Exception e) {
            logger.error("Failed to update medication (ID: {}): {}", id, e.getMessage(), e);
            model.addAttribute("error", "Error updating medication: " + e.getMessage());
            return "user/medication/edit";
        }
    }

    /**
     * Handles medication deletion requests.
     *
     * @param id ID of the medication to delete
     * @return Redirect to medication list with status parameter
     */
    @PostMapping("/medication/{id}/delete")
    public String deleteMedication(@PathVariable Long id) {
        logger.info("Attempting to delete medication ID: {}", id);
        try {
            medicationService.deleteMedication(id);
            logger.info("Medication deleted successfully (ID: {})", id);
            return "redirect:/user/medication?deleted";
        } catch (Exception e) {
            logger.error("Failed to delete medication (ID: {}): {}", id, e.getMessage(), e);
            return "redirect:/user/medication?error";
        }
    }

    /**
     * Displays upcoming medication refills.
     *
     * @param userDetails Authenticated user details
     * @param model Spring MVC model for view data
     * @return The refills view template
     */
    @GetMapping("/refills")
    public String showRefills(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        logger.debug("Loading upcoming refills for user: {}", userDetails.getUsername());
        try {
            model.addAttribute("refills",
                    medicationService.getUpcomingRefills(userDetails.getUsername()));
            return "user/refills";
        } catch (Exception e) {
            logger.error("Failed to load refills for user {}: {}",
                    userDetails.getUsername(), e.getMessage(), e);
            model.addAttribute("error", "Failed to load refill data");
            return "user/refills";
        }
    }

    /**
     * Displays health metrics information.
     *
     * @return The health metrics view template
     */
    @GetMapping("/health")
    public String showHealthMetrics() {
        logger.debug("Displaying health metrics view");
        return "user/health";
    }
}