package greencity.mapping;

import greencity.dto.notification.NotificationDto;
import greencity.entity.Notification;
import greencity.enums.NotificationType;
import greencity.enums.ProjectName;
import org.junit.jupiter.api.Test;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NotificationDtoMapperTest {
    private final NotificationDtoMapper mapper = new NotificationDtoMapper();

    @Test
    void convert_NotificationWithAllFields_ReturnsNotificationDto() {
        ZonedDateTime time = ZonedDateTime.now();
        Notification notification = Notification.builder()
            .id(1L)
            .notificationType(NotificationType.ECONEWS_COMMENT)
            .projectName(ProjectName.GREENCITY)
            .time(time)
            .viewed(false)
            .customMessage("custom")
            .targetId(2L)
            .secondMessageId(3L)
            .secondMessage("title")
            .build();

        NotificationDto dto = mapper.convert(notification);

        assertEquals(1L, dto.getNotificationId());
        assertEquals(ProjectName.GREENCITY.name(), dto.getProjectName());
        assertEquals(NotificationType.ECONEWS_COMMENT.name(), dto.getNotificationType());
        assertEquals(time, dto.getTime());
        assertFalse(dto.getViewed());
        assertEquals("custom", dto.getMessage());
        assertEquals(2L, dto.getTargetId());
        assertEquals(3L, dto.getSecondMessageId());
        assertEquals("title", dto.getSecondMessage());
    }
}
