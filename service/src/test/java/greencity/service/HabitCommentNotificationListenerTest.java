package greencity.service;

import greencity.dto.user.UserVO;
import greencity.enums.NotificationType;
import greencity.event.HabitCommentNotificationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static greencity.ModelUtils.getUserVO;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class HabitCommentNotificationListenerTest {
    @Mock
    private UserNotificationService userNotificationService;
    @InjectMocks
    private HabitCommentNotificationListener listener;

    @Test
    void onHabitComment_CommenterIsNotOwner_CreatesNotification() {
        UserVO owner = getUserVO();
        owner.setId(1L);
        UserVO commenter = getUserVO();
        commenter.setId(2L);
        HabitCommentNotificationEvent event = HabitCommentNotificationEvent.builder()
            .owner(owner)
            .commenter(commenter)
            .habitId(1L)
            .habitName("title")
            .build();

        listener.onHabitComment(event);

        verify(userNotificationService).createNotification(owner, commenter,
            NotificationType.HABIT_COMMENT, 1L, "title");
    }

    @Test
    void onHabitComment_CommenterIsOwner_DoesNotCreateNotification() {
        UserVO owner = getUserVO();
        owner.setId(1L);
        UserVO commenter = getUserVO();
        commenter.setId(1L);
        HabitCommentNotificationEvent event = HabitCommentNotificationEvent.builder()
            .owner(owner)
            .commenter(commenter)
            .habitId(1L)
            .habitName("title")
            .build();

        listener.onHabitComment(event);

        verifyNoInteractions(userNotificationService);
    }
}
