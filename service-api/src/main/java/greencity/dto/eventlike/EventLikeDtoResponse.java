package greencity.dto.eventlike;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventLikeDtoResponse {
    @NotNull
    @Min(1)
    private Long eventId;

    @NotNull
    @Min(0)
    private Long likesCount;

    @NotNull
    private Boolean liked;
}
