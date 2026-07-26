package greencity.application.event;

import greencity.dto.user.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EventCreatedNotificationEvent {
    private final UserVO organizer;
    private final Long eventId;
    private final String eventTitle;
}
