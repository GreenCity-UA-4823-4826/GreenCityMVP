package greencity.event;

import greencity.dto.user.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class HabitCommentNotificationEvent {
    private final UserVO owner;
    private final UserVO commenter;
    private final Long habitId;
    private final String habitName;
}
