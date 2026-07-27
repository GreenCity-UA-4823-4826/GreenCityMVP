package greencity.service;

import greencity.event.HabitCommentNotificationEvent;
import greencity.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class HabitCommentNotificationListener {
    private final UserNotificationService userNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onHabitComment(HabitCommentNotificationEvent event) {
        if (event.getOwner().getId().equals(event.getCommenter().getId())) {
            return;
        }
        userNotificationService.createNotification(
            event.getOwner(),
            event.getCommenter(),
            NotificationType.HABIT_COMMENT,
            event.getHabitId(),
            event.getHabitName());
    }
}
