package greencity.dto.newssubscriber;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class NewsSubscriberRequestDto {
    @NotBlank(message = "Email is required")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "Email should be valid")
    private String email;

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }
}
