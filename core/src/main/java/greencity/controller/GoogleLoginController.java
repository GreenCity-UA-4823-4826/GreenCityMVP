package greencity.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth/google")
@RequiredArgsConstructor
public class GoogleLoginController {
    private final OAuth2AuthorizedClientService authorizedClientService;

    @GetMapping("/login")
    public String login() {
        return "redirect:/oauth2/authorization/google";
    }

    @GetMapping("/success")
    public String loginSuccess(Authentication authentication, HttpServletRequest request,
                               HttpServletResponse response) {
        // Handle successful Google login
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            String email = oauthToken.getPrincipal().getAttributes().get("email").toString();
            // Redirect to registration page with email pre-filled
            return "redirect:/register?email=" + email;
        }
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            authorizedClientService.removeAuthorizedClient(
                    authentication.getAuthorities().toString(),
                    authentication.getName());
        }
        return "redirect:/login";
    }
}