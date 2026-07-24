package greencity.service;

import greencity.event.EcoNewsCommentNotificationEvent;
import greencity.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EcoNewsCommentNotificationListener {
    private final UserNotificationService userNotificationService;

    @Async
    @EventListener
    public void onEcoNewsComment(EcoNewsCommentNotificationEvent event) {
        if (event.getAuthor().getId().equals(event.getCommenter().getId())) {
            return;
        }
        userNotificationService.createNotification(
            event.getAuthor(),
            event.getCommenter(),
            NotificationType.ECONEWS_COMMENT,
            event.getEcoNewsId(),
            event.getNewsTitle(),
            event.getEcoNewsId());
    }
}
