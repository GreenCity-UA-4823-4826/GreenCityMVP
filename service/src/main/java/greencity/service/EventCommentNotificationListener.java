package greencity.service;

import greencity.event.EventCommentNotificationEvent;
import greencity.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventCommentNotificationListener {
    private final UserNotificationService userNotificationService;

    @Async
    @EventListener
    public void onEventComment(EventCommentNotificationEvent event) {
        if (event.getOrganizer().getId().equals(event.getCommenter().getId())) {
            return;
        }
        userNotificationService.createNotification(
            event.getOrganizer(),
            event.getCommenter(),
            NotificationType.EVENT_COMMENT,
            event.getEventId(),
            event.getEventTitle());
    }
}
