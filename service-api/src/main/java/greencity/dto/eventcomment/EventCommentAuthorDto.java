package greencity.dto.eventcomment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class EventCommentAuthorDto {
    @NotEmpty
    private Long id;

    @NotEmpty
    private String name;

    private String userProfilePicturePath;
}
