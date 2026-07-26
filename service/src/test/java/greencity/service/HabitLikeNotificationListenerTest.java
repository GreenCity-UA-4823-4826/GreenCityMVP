package greencity.service;

import greencity.dto.user.UserVO;
import greencity.enums.NotificationType;
import greencity.event.HabitLikeNotificationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static greencity.ModelUtils.getUserVO;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class HabitLikeNotificationListenerTest {
    @Mock
    private UserNotificationService userNotificationService;
    @InjectMocks
    private HabitLikeNotificationListener listener;

    @Test
    void onHabitLike_LikerIsNotOwnerAndLiked_CreatesNotification() {
        UserVO owner = getUserVO();
        owner.setId(1L);
        UserVO liker = getUserVO();
        liker.setId(2L);
        HabitLikeNotificationEvent event = HabitLikeNotificationEvent.builder()
            .owner(owner)
            .liker(liker)
            .habitId(1L)
            .habitName("title")
            .liked(true)
            .build();

        listener.onHabitLike(event);

        verify(userNotificationService).createNotification(owner, liker,
            NotificationType.HABIT_LIKE, 1L, "title");
    }

    @Test
    void onHabitLike_LikerIsNotOwnerAndUnliked_RemovesActionUser() {
        UserVO owner = getUserVO();
        owner.setId(1L);
        UserVO liker = getUserVO();
        liker.setId(2L);
        HabitLikeNotificationEvent event = HabitLikeNotificationEvent.builder()
            .owner(owner)
            .liker(liker)
            .habitId(1L)
            .habitName("title")
            .liked(false)
            .build();

        listener.onHabitLike(event);

        verify(userNotificationService).removeActionUser(owner, liker, NotificationType.HABIT_LIKE, 1L);
    }

    @Test
    void onHabitLike_LikerIsOwner_DoesNotInteractWithNotificationService() {
        UserVO owner = getUserVO();
        owner.setId(1L);
        UserVO liker = getUserVO();
        liker.setId(1L);
        HabitLikeNotificationEvent event = HabitLikeNotificationEvent.builder()
            .owner(owner)
            .liker(liker)
            .habitId(1L)
            .habitName("title")
            .liked(true)
            .build();

        listener.onHabitLike(event);

        verifyNoInteractions(userNotificationService);
    }
}
