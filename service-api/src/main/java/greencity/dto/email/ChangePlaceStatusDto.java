package greencity.dto.email;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePlaceStatusDto {
    @NotBlank
    private String authorEmail;

    @NotBlank
    private String authorFirstName;

    @NotBlank
    private String placeName;

    @NotBlank
    private String placeStatus;
}
