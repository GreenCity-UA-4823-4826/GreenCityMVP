package greencity.event;

import greencity.dto.user.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class HabitInviteNotificationEvent {
    private final UserVO invitee;
    private final UserVO inviter;
    private final Long habitId;
    private final String habitName;
    private final boolean active;
}
