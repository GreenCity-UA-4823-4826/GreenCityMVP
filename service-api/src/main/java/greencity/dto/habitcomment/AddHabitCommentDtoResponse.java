package greencity.dto.habitcomment;

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
public class AddHabitCommentDtoResponse {
    @NotNull
    @Min(1)
    private Long id;

    @NotNull
    @Valid
    private HabitCommentAuthorDto author;

    @NotEmpty
    private String text;

    @NotNull
    private LocalDateTime createdDate;
}
