//This controller "listens" for user responses.


package com.jydoc.deliverable3.Controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;




@Controller
public class indexController {

    // This method maps to the root URL ("/")
    @GetMapping("/")
    public String index(Model model) {
        // Add the user's name to the model (this can be dynamic)
        model.addAttribute("userName", "Guest");

        // Add the current date to the model
        model.addAttribute("currentDate", LocalDate.now());

        // Return the name of the Thymeleaf template ("index")
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username, @RequestParam String password, Model model) {
        //TODO: Implement User authentication
        model.addAttribute("username", username);
        model.addAttribute("password", password);

        return "loginSuccess";
    }
}
