package greencity.application.event;

import greencity.dto.user.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class EventDeletedNotificationEvent {
    private final UserVO organizer;
    private final List<UserVO> attendees;
    private final Long eventId;
    private final String eventTitle;
}
