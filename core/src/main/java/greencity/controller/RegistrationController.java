package greencity.controller;

import greencity.dto.user.GoogleUserRegistrationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class RegistrationController {
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new GoogleUserRegistrationDto());
        return "registration";
    }

    @PostMapping("/register")
    public String registerUser(@Valid GoogleUserRegistrationDto dto, Model model) {
        // TODO Here you would call your service to handle registration
        // For now we're just returning the form for demonstration
        return "redirect:/login?registered";
    }
}