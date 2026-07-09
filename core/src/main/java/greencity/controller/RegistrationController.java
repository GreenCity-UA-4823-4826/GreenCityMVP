package greencity.controller;

import greencity.dto.user.UserGoogleRegistrationDto;
import greencity.service.GoogleUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class RegistrationController {
    private final GoogleUserService googleUserService;

    @GetMapping("/register")
    public String showRegistrationForm(UserGoogleRegistrationDto dto, Model model) {
        model.addAttribute("user", dto);
        return "registration";
    }

    @PostMapping("/register")
    public String registerUser(@Valid UserGoogleRegistrationDto dto, Model model) {
        try {
            googleUserService.registerGoogleUser(dto);
            return "redirect:/login?registered";
        } catch (RuntimeException e) {
            model.addAttribute("user", dto);
            model.addAttribute("error", e.getMessage());
            return "registration";
        }
    }
}
