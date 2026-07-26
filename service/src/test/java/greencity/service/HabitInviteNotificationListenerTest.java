package greencity.service;

import greencity.dto.user.UserVO;
import greencity.enums.NotificationType;
import greencity.event.HabitInviteNotificationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static greencity.ModelUtils.getUserVO;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HabitInviteNotificationListenerTest {
    @Mock
    private UserNotificationService userNotificationService;
    @InjectMocks
    private HabitInviteNotificationListener listener;

    @Test
    void onHabitInvite_ActiveTrue_CreatesNotification() {
        UserVO invitee = getUserVO();
        invitee.setId(1L);
        UserVO inviter = getUserVO();
        inviter.setId(2L);
        HabitInviteNotificationEvent event = HabitInviteNotificationEvent.builder()
            .invitee(invitee)
            .inviter(inviter)
            .habitId(1L)
            .habitName("title")
            .active(true)
            .build();

        listener.onHabitInvite(event);

        verify(userNotificationService).createNotification(invitee, inviter,
            NotificationType.HABIT_INVITE, 1L, "title");
    }

    @Test
    void onHabitInvite_ActiveFalse_RemovesActionUser() {
        UserVO invitee = getUserVO();
        invitee.setId(1L);
        UserVO inviter = getUserVO();
        inviter.setId(2L);
        HabitInviteNotificationEvent event = HabitInviteNotificationEvent.builder()
            .invitee(invitee)
            .inviter(inviter)
            .habitId(1L)
            .habitName("title")
            .active(false)
            .build();

        listener.onHabitInvite(event);

        verify(userNotificationService).removeActionUser(invitee, inviter, NotificationType.HABIT_INVITE, 1L);
    }
}
