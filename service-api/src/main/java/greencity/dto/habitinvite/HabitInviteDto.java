package greencity.dto.habitinvite;

import greencity.enums.HabitInviteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class HabitInviteDto {
    @NotNull
    @Min(1)
    private Long id;

    private Long habitId;

    private String habitName;

    private Long inviterId;

    private Long inviteeId;

    private HabitInviteStatus status;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;
}
