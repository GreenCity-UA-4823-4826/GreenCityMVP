package greencity.controller;

import greencity.dto.user.UserGoogleRegistrationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/google")
@RequiredArgsConstructor
public class GoogleAuthController {
    private final GoogleUserServiceImpl googleUserService;

    @PostMapping("/register")
    public ResponseEntity<?> registerGoogleUser(@Valid @RequestBody UserGoogleRegistrationDto dto) {
        try {
            var user = googleUserService.registerGoogleUser(dto);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}