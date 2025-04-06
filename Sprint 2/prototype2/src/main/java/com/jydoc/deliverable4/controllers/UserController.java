package com.jydoc.deliverable4.controllers;

import com.jydoc.deliverable4.services.UserService;
import com.jydoc.deliverable4.services.MedicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasRole('ROLE_USER')")
public class UserController {

    private final UserService userService;
    private final MedicationService medicationService;

    public UserController(UserService userService, MedicationService medicationService) {
        this.userService = userService;
        this.medicationService = medicationService;
    }

//    @GetMapping
//    public String userProfile(Model model, Principal principal) {
//        User user = userService.findUserByCredential(principal.getName());
//        model.addAttribute("user", user);
//        model.addAttribute("medicationCount", medicationService.getUserMedications(user).size());
//        return "user/profile";
//    }
//
//    @GetMapping("/edit")
//    public String editProfileForm(Model model, Principal principal) {
//        User user = userService.findUserByCredential(principal.getName());
//        model.addAttribute("user", user);
//        return "user/edit";
//    }
//
//    @PostMapping("/edit")
//    public String updateProfile(@Valid @ModelAttribute("user") User updatedUser,
//                                BindingResult result,
//                                Principal principal) {
//        if (result.hasErrors()) {
//            return "user/edit";
//        }
//
//        User currentUser = userService.findUserByCredential(principal.getName());
//        userService.updateUser(currentUser, updatedUser);
//        return "redirect:/user?updated";
//    }

//    @GetMapping("/change-password")
//    public String changePasswordForm() {
//        return "user/change-password";
//    }
//
//    @PostMapping("/change-password")
//    public String changePassword(@RequestParam String currentPassword,
//                                 @RequestParam String newPassword,
//                                 @RequestParam String confirmPassword,
//                                 Principal principal,
//                                 RedirectAttributes redirectAttributes) {
//        try {
//            userService.changePassword(principal.getName(), currentPassword, newPassword, confirmPassword);
//            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
//            return "redirect:/user";
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//            return "redirect:/user/change-password";
//        }
//    }
}