package greencity.service;

import greencity.event.HabitLikeNotificationEvent;
import greencity.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class HabitLikeNotificationListener {
    private final UserNotificationService userNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onHabitLike(HabitLikeNotificationEvent event) {
        if (event.getOwner().getId().equals(event.getLiker().getId())) {
            return;
        }
        if (event.isLiked()) {
            userNotificationService.createNotification(
                event.getOwner(),
                event.getLiker(),
                NotificationType.HABIT_LIKE,
                event.getHabitId(),
                event.getHabitName());
        } else {
            userNotificationService.removeActionUser(
                event.getOwner(),
                event.getLiker(),
                NotificationType.HABIT_LIKE,
                event.getHabitId());
        }
    }
}
