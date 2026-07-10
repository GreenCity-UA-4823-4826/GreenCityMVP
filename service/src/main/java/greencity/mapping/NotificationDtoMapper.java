package greencity.mapping;

import greencity.dto.notification.NotificationDto;
import greencity.entity.Notification;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class NotificationDtoMapper extends AbstractConverter<Notification, NotificationDto> {
    @Override
    protected NotificationDto convert(Notification notification) {
        return NotificationDto.builder()
            .notificationId(notification.getId())
            .projectName(notification.getProjectName().name())
            .notificationType(notification.getNotificationType().name())
            .time(notification.getTime())
            .viewed(notification.isViewed())
            .message(notification.getCustomMessage())
            .targetId(notification.getTargetId())
            .secondMessageId(notification.getSecondMessageId())
            .secondMessage(notification.getSecondMessage())
            .build();
    }
}
