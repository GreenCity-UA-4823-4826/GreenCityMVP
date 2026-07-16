package greencity.service;

import greencity.dto.user.UserVO;
import greencity.enums.NotificationType;
import greencity.event.EcoNewsCommentNotificationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static greencity.ModelUtils.getUserVO;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EcoNewsCommentNotificationListenerTest {
    @Mock
    private UserNotificationService userNotificationService;
    @InjectMocks
    private EcoNewsCommentNotificationListener listener;

    @Test
    void onEcoNewsComment_CommenterIsNotAuthor_CreatesNotification() {
        UserVO author = getUserVO();
        UserVO commenter = getUserVO();
        commenter.setId(2L);
        EcoNewsCommentNotificationEvent event = EcoNewsCommentNotificationEvent.builder()
            .author(author)
            .commenter(commenter)
            .ecoNewsId(1L)
            .newsTitle("title")
            .build();

        listener.onEcoNewsComment(event);

        verify(userNotificationService).createNotification(author, commenter,
            NotificationType.ECONEWS_COMMENT, 1L, "title", 1L);
    }

    @Test
    void onEcoNewsComment_CommenterIsAuthor_DoesNotCreateNotification() {
        EcoNewsCommentNotificationEvent event = EcoNewsCommentNotificationEvent.builder()
            .author(getUserVO())
            .commenter(getUserVO())
            .ecoNewsId(1L)
            .newsTitle("title")
            .build();

        listener.onEcoNewsComment(event);

        verifyNoInteractions(userNotificationService);
    }
}
