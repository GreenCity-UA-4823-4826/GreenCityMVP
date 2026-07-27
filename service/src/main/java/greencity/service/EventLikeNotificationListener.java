package greencity.service;

import greencity.event.EventLikeNotificationEvent;
import greencity.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EventLikeNotificationListener {
    private final UserNotificationService userNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEventLike(EventLikeNotificationEvent event) {
        if (event.getOrganizer().getId().equals(event.getLiker().getId())) {
            return;
        }
        if (event.isLiked()) {
            userNotificationService.createNotification(
                event.getOrganizer(),
                event.getLiker(),
                NotificationType.EVENT_LIKE,
                event.getEventId(),
                event.getEventTitle());
        } else {
            userNotificationService.removeActionUser(
                event.getOrganizer(),
                event.getLiker(),
                NotificationType.EVENT_LIKE,
                event.getEventId());
        }
    }
}
