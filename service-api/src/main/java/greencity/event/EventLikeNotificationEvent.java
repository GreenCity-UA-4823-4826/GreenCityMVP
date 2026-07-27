package greencity.event;

import greencity.dto.user.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EventLikeNotificationEvent {
    private final UserVO organizer;
    private final UserVO liker;
    private final Long eventId;
    private final String eventTitle;
    private final boolean liked;
}
