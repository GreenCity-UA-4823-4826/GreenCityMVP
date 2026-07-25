package greencity.event;

import greencity.dto.user.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EventCommentNotificationEvent {
    private final UserVO organizer;
    private final UserVO commenter;
    private final Long eventId;
    private final String eventTitle;
}
