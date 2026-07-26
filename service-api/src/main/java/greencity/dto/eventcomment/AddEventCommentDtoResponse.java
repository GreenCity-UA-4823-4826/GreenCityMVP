package greencity.dto.eventcomment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddEventCommentDtoResponse {
    @NotNull
    @Min(1)
    private Long id;

    @NotNull
    @Valid
    private EventCommentAuthorDto author;

    @NotEmpty
    private String text;

    @NotNull
    private LocalDateTime createdDate;
}
