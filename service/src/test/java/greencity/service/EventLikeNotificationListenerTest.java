package greencity.service;

import greencity.dto.user.UserVO;
import greencity.enums.NotificationType;
import greencity.event.EventLikeNotificationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static greencity.ModelUtils.getUserVO;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EventLikeNotificationListenerTest {
    @Mock
    private UserNotificationService userNotificationService;
    @InjectMocks
    private EventLikeNotificationListener listener;

    @Test
    void onEventLike_LikerIsNotOrganizerAndLiked_CreatesNotification() {
        UserVO organizer = getUserVO();
        organizer.setId(1L);
        UserVO liker = getUserVO();
        liker.setId(2L);
        EventLikeNotificationEvent event = EventLikeNotificationEvent.builder()
            .organizer(organizer)
            .liker(liker)
            .eventId(1L)
            .eventTitle("title")
            .liked(true)
            .build();

        listener.onEventLike(event);

        verify(userNotificationService).createNotification(organizer, liker,
            NotificationType.EVENT_LIKE, 1L, "title");
    }

    @Test
    void onEventLike_LikerIsNotOrganizerAndUnliked_RemovesActionUser() {
        UserVO organizer = getUserVO();
        organizer.setId(1L);
        UserVO liker = getUserVO();
        liker.setId(2L);
        EventLikeNotificationEvent event = EventLikeNotificationEvent.builder()
            .organizer(organizer)
            .liker(liker)
            .eventId(1L)
            .eventTitle("title")
            .liked(false)
            .build();

        listener.onEventLike(event);

        verify(userNotificationService).removeActionUser(organizer, liker, NotificationType.EVENT_LIKE, 1L);
    }

    @Test
    void onEventLike_LikerIsOrganizer_DoesNotInteractWithNotificationService() {
        UserVO organizer = getUserVO();
        organizer.setId(1L);
        UserVO liker = getUserVO();
        liker.setId(1L);
        EventLikeNotificationEvent event = EventLikeNotificationEvent.builder()
            .organizer(organizer)
            .liker(liker)
            .eventId(1L)
            .eventTitle("title")
            .liked(true)
            .build();

        listener.onEventLike(event);

        verifyNoInteractions(userNotificationService);
    }
}
