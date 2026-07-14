package greencity.dto.newssubscriber;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsSubscriberRequestDto {
    @NotBlank
    @Email
    private String email;
}
