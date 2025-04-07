package com.jydoc.deliverable4.controllers;

import com.jydoc.deliverable4.dtos.DashboardDTO;
import com.jydoc.deliverable4.dtos.MedicationDTO;
import com.jydoc.deliverable4.services.DashboardService;
import com.jydoc.deliverable4.services.MedicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import javax.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final DashboardService dashboardService;
    private final MedicationService medicationService;

    public UserController(DashboardService dashboardService,
                          MedicationService medicationService) {
        this.dashboardService = dashboardService;
        this.medicationService = medicationService;
        logger.info("UserController initialized");
    }

    // Dashboard and Profile Endpoints (unchanged)
    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        logger.debug("Loading dashboard for user: {}", userDetails.getUsername());
        DashboardDTO dashboard = dashboardService.getUserDashboardData(userDetails);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("hasMedications", dashboardService.hasMedications(userDetails));
        return "user/dashboard";
    }

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails) {
        logger.debug("Loading profile for user: {}", userDetails.getUsername());
        return "user/profile";
    }

    @GetMapping("/medication")
    public String showMedications(@AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {
        logger.debug("Loading medications for user: {}", userDetails.getUsername());
        model.addAttribute("medications",
                medicationService.getUserMedications(userDetails.getUsername()));
        return "user/medication/list";
    }


    // Medication Management Endpoints
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
            // Return to form with error (model will preserve the entered data)
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

    // Schedule and Refills Endpoints
//    @GetMapping("/schedule")
//    public String showSchedule(@AuthenticationPrincipal UserDetails userDetails, Model model) {
//        model.addAttribute("schedule",
//                medicationService.getMedicationSchedule(userDetails.getUsername()));
//        return "user/schedule";
//    }

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
}