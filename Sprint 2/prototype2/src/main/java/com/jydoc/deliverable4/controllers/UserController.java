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

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$";

    private final DashboardService dashboardService;
    private final MedicationService medicationService;
    private final UserService userService;  // Added UserService

    public UserController(DashboardService dashboardService,
                          MedicationService medicationService,
                          UserService userService) {
        this.dashboardService = dashboardService;
        this.medicationService = medicationService;
        this.userService = userService;
        logger.info("UserController initialized");
    }

    // Existing Endpoints (unchanged)
    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        logger.debug("Loading dashboard for user: {}", userDetails.getUsername());
        DashboardDTO dashboard = dashboardService.getUserDashboardData(userDetails);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("hasMedications", dashboardService.hasMedications(userDetails));
        return "user/dashboard";
    }

    // Modified Profile Endpoints
    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        logger.debug("Loading profile for user: {}", userDetails.getUsername());
        UserDTO user = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @Valid @ModelAttribute("user") UserDTO userDTO,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            logger.warn("Validation errors in profile update: {}", result.getAllErrors());
            return "user/profile";
        }

        try {
            userService.updateUserProfile(userDetails.getUsername(), userDTO);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } catch (Exception e) {
            logger.error("Error updating profile: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to update profile");
        }

        return "redirect:/user/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam("currentPassword") @NotBlank String currentPassword,
            @RequestParam("newPassword") @NotBlank String newPassword,
            @RequestParam("confirmPassword") @NotBlank String confirmPassword,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        // Validate password match first
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match");
            return "redirect:/user/profile";
        }

        try {
            userService.changePassword(
                    userDetails.getUsername(),
                    currentPassword,
                    newPassword
            );
            redirectAttributes.addFlashAttribute("success", "Password changed successfully");
        } catch (PasswordMismatchException e) {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
        } catch (WeakPasswordException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            logger.error("Password change failed for user: {}", userDetails.getUsername(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to change password");
        }

        return "redirect:/user/profile";
    }


    @PostMapping("/profile/delete")
    public String deleteAccount(
            @RequestParam("deletePassword") String password,
            @RequestParam("confirmDelete") boolean confirmDelete,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (!confirmDelete) {
            redirectAttributes.addFlashAttribute("error", "Please confirm account deletion");
            return "redirect:/user/profile";
        }

        try {
            boolean deleted = userService.deleteAccount(userDetails.getUsername(), password);
            if (deleted) {
                return "redirect:/auth/logout";
            } else {
                redirectAttributes.addFlashAttribute("error", "Incorrect password");
            }
        } catch (Exception e) {
            logger.error("Error deleting account: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to delete account");
        }
        return "redirect:/user/profile";
    }

    // Existing Medication Endpoints (unchanged)
    @GetMapping("/medication")
    public String showMedications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        logger.debug("Loading medications for user: {}", userDetails.getUsername());
        model.addAttribute("medications",
                medicationService.getUserMedications(userDetails.getUsername()));
        return "user/medication/list";
    }

    @GetMapping("/medication/list")
    public String showMedicationList(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        logger.debug("Loading medications for user: {}", userDetails.getUsername());
        model.addAttribute("medications",
                medicationService.getUserMedications(userDetails.getUsername()));
        return "user/medication/list";
    }

    @GetMapping("/medication/add")
    public String showAddMedicationForm(Model model) {
        model.addAttribute("medicationDTO", new MedicationDTO());
        return "user/medication/add";
    }

    @PostMapping("/medication")
    public String addMedication(
            @Valid @ModelAttribute("medicationDTO") MedicationDTO medicationDTO,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (result.hasErrors()) {
            logger.warn("Validation errors: {}", result.getAllErrors());
            return "user/medication/add";
        }
        try {
            medicationService.createMedication(medicationDTO, userDetails.getUsername());
            return "redirect:/user/medication?success";
        } catch (Exception e) {
            logger.error("Error adding medication: {}", e.getMessage());
            return "user/medication/add";
        }
    }

    @GetMapping("/medication/{id}/edit")
    public String showEditMedicationForm(@PathVariable Long id, Model model) {
        MedicationDTO medicationDTO = medicationService.getMedicationById(id);
        model.addAttribute("medicationDTO", medicationDTO);
        return "user/medication/edit";
    }

    @PostMapping("/medication/{id}")
    public String updateMedication(
            @PathVariable Long id,
            @Valid @ModelAttribute("medicationDTO") MedicationDTO medicationDTO,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            return "user/medication/edit";
        }
        try {
            medicationService.updateMedication(id, medicationDTO);
            return "redirect:/user/medication?updated";
        } catch (Exception e) {
            model.addAttribute("error", "Error updating medication: " + e.getMessage());
            return "user/medication/edit";
        }
    }

    @PostMapping("/medication/{id}/delete")
    public String deleteMedication(@PathVariable Long id) {
        try {
            medicationService.deleteMedication(id);
            return "redirect:/user/medication?deleted";
        } catch (Exception e) {
            logger.error("Error deleting medication: {}", e.getMessage());
            return "redirect:/user/medication?error";
        }
    }

    // Existing Schedule and Refills Endpoints (unchanged)
    @GetMapping("/refills")
    public String showRefills(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("refills",
                medicationService.getUpcomingRefills(userDetails.getUsername()));
        return "user/refills";
    }

    @GetMapping("/health")
    public String showHealthMetrics() {
        return "user/health";
    }

    // Helper method for password validation
    private boolean isValidPassword(String password) {
        if (password.length() < 6) return false;

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasNumber = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUppercase = true;
            if (Character.isLowerCase(c)) hasLowercase = true;
            if (Character.isDigit(c)) hasNumber = true;
        }

        return hasUppercase && hasLowercase && hasNumber;
    }
}