package greencity.application.event;

import greencity.dto.user.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EcoNewsCommentNotificationEvent {
    private final UserVO author;
    private final UserVO commenter;
    private final Long ecoNewsId;
    private final String newsTitle;
}
