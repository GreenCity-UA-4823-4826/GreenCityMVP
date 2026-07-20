package greencity.listeners;

import greencity.enums.NotificationType;
import greencity.application.event.EventDeletedNotificationEvent;
import greencity.application.event.EventUpdatedNotificationEvent;
import greencity.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventNotificationListener {
    private final UserNotificationService userNotificationService;

    @Async
    @EventListener
    public void onEventUpdated(EventUpdatedNotificationEvent event) {
        event.getAttendees().forEach(attendee -> userNotificationService.createNotification(
            attendee,
            event.getOrganizer(),
            NotificationType.EVENT_EDITED,
            event.getEventId(),
            event.getEventTitle()));
    }

    @Async
    @EventListener
    public void onEventDeleted(EventDeletedNotificationEvent event) {
        event.getAttendees().forEach(attendee -> userNotificationService.createNotification(
                attendee,
                event.getOrganizer(),
                NotificationType.EVENT_CANCELLED,
                event.getEventId(),
                event.getEventTitle()));
    }
}
