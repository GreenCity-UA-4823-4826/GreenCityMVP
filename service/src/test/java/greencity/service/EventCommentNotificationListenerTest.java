package greencity.service;

import greencity.dto.user.UserVO;
import greencity.enums.NotificationType;
import greencity.event.EventCommentNotificationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static greencity.ModelUtils.getUserVO;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EventCommentNotificationListenerTest {
    @Mock
    private UserNotificationService userNotificationService;
    @InjectMocks
    private EventCommentNotificationListener listener;

    @Test
    void onEventComment_CommenterIsNotOrganizer_CreatesNotification() {
        UserVO organizer = getUserVO();
        UserVO commenter = getUserVO();
        commenter.setId(2L);
        EventCommentNotificationEvent event = EventCommentNotificationEvent.builder()
            .organizer(organizer)
            .commenter(commenter)
            .eventId(1L)
            .eventTitle("title")
            .build();

        listener.onEventComment(event);

        verify(userNotificationService).createNotification(organizer, commenter,
            NotificationType.EVENT_COMMENT, 1L, "title");
    }

    @Test
    void onEventComment_CommenterIsOrganizer_DoesNotCreateNotification() {
        EventCommentNotificationEvent event = EventCommentNotificationEvent.builder()
            .organizer(getUserVO())
            .commenter(getUserVO())
            .eventId(1L)
            .eventTitle("title")
            .build();

        listener.onEventComment(event);

        verifyNoInteractions(userNotificationService);
    }
}
