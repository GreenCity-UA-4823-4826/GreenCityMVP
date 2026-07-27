package greencity.event;

import greencity.dto.user.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class HabitLikeNotificationEvent {
    private final UserVO owner;
    private final UserVO liker;
    private final Long habitId;
    private final String habitName;
    private final boolean liked;
}
