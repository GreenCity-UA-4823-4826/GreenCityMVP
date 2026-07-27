package greencity.service;

import greencity.event.HabitInviteNotificationEvent;
import greencity.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class HabitInviteNotificationListener {
    private final UserNotificationService userNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onHabitInvite(HabitInviteNotificationEvent event) {
        if (event.isActive()) {
            userNotificationService.createNotification(
                event.getInvitee(),
                event.getInviter(),
                NotificationType.HABIT_INVITE,
                event.getHabitId(),
                event.getHabitName());
        } else {
            userNotificationService.removeActionUser(
                event.getInvitee(),
                event.getInviter(),
                NotificationType.HABIT_INVITE,
                event.getHabitId());
        }
    }
}
