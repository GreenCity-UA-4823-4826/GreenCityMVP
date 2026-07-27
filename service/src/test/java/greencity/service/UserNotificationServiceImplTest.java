package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.notification.ActionDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.user.UserVO;
import greencity.entity.Notification;
import greencity.entity.User;
import greencity.enums.NotificationType;
import greencity.enums.ProjectName;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.NotificationRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.getUserVO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserNotificationServiceImplTest {
    @Mock
    private NotificationRepo notificationRepo;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @InjectMocks
    private UserNotificationServiceImpl userNotificationService;

    private Notification getNotification() {
        return Notification.builder()
            .id(1L)
            .targetUser(getUser())
            .actionUsers(new ArrayList<>(List.of(getUser())))
            .notificationType(NotificationType.ECONEWS_COMMENT)
            .projectName(ProjectName.GREENCITY)
            .targetId(1L)
            .secondMessage("title")
            .time(ZonedDateTime.now())
            .viewed(false)
            .emailSent(false)
            .build();
    }

    private User buildUser(long id, String name) {
        User user = getUser();
        user.setId(id);
        user.setName(name);
        return user;
    }

    private PageableAdvancedDto<NotificationDto> filterSingleNotification(Notification notification) {
        Pageable pageable = PageRequest.of(0, 10);
        NotificationDto dto = NotificationDto.builder()
            .notificationId(1L)
            .notificationType(NotificationType.ECONEWS_COMMENT.name())
            .build();

        when(notificationRepo.findAll(any(Specification.class), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(notification), pageable, 1));
        when(modelMapper.map(notification, NotificationDto.class)).thenReturn(dto);

        return userNotificationService.getNotificationsFiltered(
            1L, pageable, "en", ProjectName.GREENCITY, List.of(NotificationType.ECONEWS_COMMENT), false);
    }

    @Test
    void getNotificationsFiltered_SingleActionUser_ReturnsResolvedSingleUserText() {
        Notification notification = getNotification();
        notification.setSecondMessage("How to be eco friendly today");

        NotificationDto actual = filterSingleNotification(notification).getPage().getFirst();

        assertEquals("News comment", actual.getTitleText());
        assertEquals("Taras commented on your news «How to be eco friendly today».", actual.getBodyText());
        assertEquals(List.of(getUser().getId()), actual.getActionUserId());
        assertEquals(List.of("Taras"), actual.getActionUserText());
    }

    @Test
    void getNotificationsFiltered_TwoActionUsers_ReturnsResolvedTwoUsersText() {
        User maria = buildUser(2L, "Maria");
        User olha = buildUser(3L, "Olha");
        Notification notification = getNotification();
        notification.setActionUsers(new ArrayList<>(List.of(maria, olha)));
        notification.setSecondMessage("How to be eco friendly today");

        NotificationDto actual = filterSingleNotification(notification).getPage().getFirst();

        assertEquals("Maria and Olha commented on your news «How to be eco friendly today».",
            actual.getBodyText());
        assertEquals(List.of(2L, 3L), actual.getActionUserId());
        assertEquals(List.of("Maria", "Olha"), actual.getActionUserText());
    }

    @Test
    void getNotificationsFiltered_ThreeOrMoreActionUsers_ReturnsLastTwoUsersAndOtherUsersText() {
        User first = buildUser(2L, "Ivan");
        User maria = buildUser(3L, "Maria");
        User olha = buildUser(4L, "Olha");
        Notification notification = getNotification();
        notification.setActionUsers(new ArrayList<>(List.of(first, maria, olha)));
        notification.setSecondMessage("How to be eco friendly today");

        NotificationDto actual = filterSingleNotification(notification).getPage().getFirst();

        assertEquals("Maria, Olha and other users commented on your news «How to be eco friendly today».",
            actual.getBodyText());
        assertEquals(List.of(3L, 4L), actual.getActionUserId());
        assertEquals(List.of("Maria", "Olha"), actual.getActionUserText());
    }

    @Test
    void notificationSocket_UserHasUnreadNotifications_SendsCountToUserTopic() {
        ActionDto actionDto = ActionDto.builder().userId(1L).build();
        when(notificationRepo.countUnreadActionUsersByTargetUserId(1L)).thenReturn(3L);

        userNotificationService.notificationSocket(actionDto);

        verify(messagingTemplate).convertAndSend("/topic/1/notification", 3L);
    }

    @Test
    void createNotification_NoUnviewedNotificationExists_SavesNewNotification() {
        UserVO targetUserVO = getUserVO();
        UserVO actionUserVO = getUserVO();
        actionUserVO.setId(2L);
        User actionUser = getUser();
        actionUser.setId(2L);

        when(notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(
            1L, NotificationType.ECONEWS_COMMENT, 1L)).thenReturn(Optional.empty());
        when(modelMapper.map(targetUserVO, User.class)).thenReturn(getUser());
        when(modelMapper.map(actionUserVO, User.class)).thenReturn(actionUser);
        when(notificationRepo.save(any(Notification.class))).then(AdditionalAnswers.returnsFirstArg());
        when(notificationRepo.countUnreadActionUsersByTargetUserId(1L)).thenReturn(1L);

        userNotificationService.createNotification(targetUserVO, actionUserVO,
            NotificationType.ECONEWS_COMMENT, 1L, "title");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo).save(captor.capture());
        Notification saved = captor.getValue();
        assertEquals(NotificationType.ECONEWS_COMMENT, saved.getNotificationType());
        assertEquals(ProjectName.GREENCITY, saved.getProjectName());
        assertEquals(1L, saved.getTargetId());
        assertEquals("title", saved.getSecondMessage());
        assertEquals(List.of(actionUser), saved.getActionUsers());
        assertFalse(saved.isViewed());
        verify(messagingTemplate).convertAndSend("/topic/1/notification", 1L);
    }

    @Test
    void createNotification_UnviewedNotificationExists_AddsActionUserToExistingNotification() {
        UserVO targetUserVO = getUserVO();
        UserVO actionUserVO = getUserVO();
        actionUserVO.setId(2L);
        User actionUser = getUser();
        actionUser.setId(2L);
        Notification existing = getNotification();

        when(notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(
            1L, NotificationType.ECONEWS_COMMENT, 1L)).thenReturn(Optional.of(existing));
        when(modelMapper.map(actionUserVO, User.class)).thenReturn(actionUser);
        when(notificationRepo.save(existing)).thenReturn(existing);
        when(notificationRepo.countUnreadActionUsersByTargetUserId(1L)).thenReturn(1L);

        userNotificationService.createNotification(targetUserVO, actionUserVO,
            NotificationType.ECONEWS_COMMENT, 1L, "new title");

        assertEquals(2, existing.getActionUsers().size());
        assertTrue(existing.getActionUsers().contains(actionUser));
        assertEquals("new title", existing.getSecondMessage());
        verify(notificationRepo).save(existing);
        verify(modelMapper, never()).map(targetUserVO, User.class);
    }

    @Test
    void createNotification_SameActionUserRepeatsAction_DoesNotDuplicateActionUser() {
        UserVO targetUserVO = getUserVO();
        UserVO actionUserVO = getUserVO();
        Notification existing = getNotification();

        when(notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(
            1L, NotificationType.ECONEWS_COMMENT, 1L)).thenReturn(Optional.of(existing));
        when(modelMapper.map(actionUserVO, User.class)).thenReturn(getUser());
        when(notificationRepo.save(existing)).thenReturn(existing);
        when(notificationRepo.countUnreadActionUsersByTargetUserId(1L)).thenReturn(1L);

        userNotificationService.createNotification(targetUserVO, actionUserVO,
            NotificationType.ECONEWS_COMMENT, 1L, "title");

        assertEquals(1, existing.getActionUsers().size());
    }

    @Test
    void removeActionUser_NotificationHasOtherActionUsers_RemovesActionUserAndSaves() {
        UserVO targetUserVO = getUserVO();
        UserVO actionUserVO = getUserVO();
        actionUserVO.setId(2L);
        User remainingUser = buildUser(3L, "Olha");
        Notification existing = getNotification();
        existing.setActionUsers(new ArrayList<>(List.of(buildUser(2L, "Maria"), remainingUser)));

        when(notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(
            1L, NotificationType.EVENT_LIKE, 1L)).thenReturn(Optional.of(existing));
        when(notificationRepo.countUnreadActionUsersByTargetUserId(1L)).thenReturn(1L);

        userNotificationService.removeActionUser(targetUserVO, actionUserVO, NotificationType.EVENT_LIKE, 1L);

        assertEquals(List.of(remainingUser), existing.getActionUsers());
        verify(notificationRepo).save(existing);
        verify(notificationRepo, never()).delete(existing);
        verify(messagingTemplate).convertAndSend("/topic/1/notification", 1L);
    }

    @Test
    void removeActionUser_NotificationBecomesEmpty_DeletesNotification() {
        UserVO targetUserVO = getUserVO();
        UserVO actionUserVO = getUserVO();
        Notification existing = getNotification();
        existing.setActionUsers(new ArrayList<>(List.of(getUser())));

        when(notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(
            1L, NotificationType.EVENT_LIKE, 1L)).thenReturn(Optional.of(existing));
        when(notificationRepo.countUnreadActionUsersByTargetUserId(1L)).thenReturn(0L);

        userNotificationService.removeActionUser(targetUserVO, actionUserVO, NotificationType.EVENT_LIKE, 1L);

        verify(notificationRepo).delete(existing);
        verify(notificationRepo, never()).save(existing);
        verify(messagingTemplate).convertAndSend("/topic/1/notification", 0L);
    }

    @Test
    void removeActionUser_NoUnviewedNotificationExists_DoesNothing() {
        UserVO targetUserVO = getUserVO();
        UserVO actionUserVO = getUserVO();

        when(notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(
            1L, NotificationType.EVENT_LIKE, 1L)).thenReturn(Optional.empty());

        userNotificationService.removeActionUser(targetUserVO, actionUserVO, NotificationType.EVENT_LIKE, 1L);

        verify(notificationRepo, never()).save(any(Notification.class));
        verify(notificationRepo, never()).delete(any(Notification.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Long.class));
    }

    @Test
    void deleteNotification_NotificationBelongsToUser_DeletesNotification() {
        when(notificationRepo.existsByIdAndTargetUserId(1L, 1L)).thenReturn(true);
        when(notificationRepo.countUnreadActionUsersByTargetUserId(1L)).thenReturn(0L);

        userNotificationService.deleteNotification(1L, 1L);

        verify(notificationRepo).deleteNotificationByIdAndTargetUserId(1L, 1L);
        verify(messagingTemplate).convertAndSend("/topic/1/notification", 0L);
    }

    @Test
    void deleteNotification_NotificationDoesNotBelongToUser_ThrowsNotFoundException() {
        when(notificationRepo.existsByIdAndTargetUserId(1L, 1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userNotificationService.deleteNotification(1L, 1L));
        verify(notificationRepo, never()).deleteNotificationByIdAndTargetUserId(1L, 1L);
    }

    @Test
    void viewNotification_NotificationBelongsToUser_MarksViewedAndSendsUnreadCount() {
        when(notificationRepo.markNotificationAsViewedByIdAndTargetUserId(1L, 1L)).thenReturn(1);
        when(notificationRepo.countUnreadActionUsersByTargetUserId(1L)).thenReturn(0L);

        userNotificationService.viewNotification(1L, 1L);

        verify(notificationRepo).markNotificationAsViewedByIdAndTargetUserId(1L, 1L);
        verify(messagingTemplate).convertAndSend("/topic/1/notification", 0L);
    }

    @Test
    void viewNotification_NotificationMissing_ThrowsNotFoundException() {
        when(notificationRepo.markNotificationAsViewedByIdAndTargetUserId(1L, 1L)).thenReturn(0);

        assertThrows(NotFoundException.class, () -> userNotificationService.viewNotification(1L, 1L));
        verify(notificationRepo, never()).countUnreadActionUsersByTargetUserId(1L);
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Long.class));
    }

    @Test
    void viewNotification_NotificationDoesNotBelongToUser_ThrowsNotFoundException() {
        when(notificationRepo.markNotificationAsViewedByIdAndTargetUserId(1L, 2L)).thenReturn(0);

        assertThrows(NotFoundException.class, () -> userNotificationService.viewNotification(2L, 1L));
        verify(notificationRepo, never()).countUnreadActionUsersByTargetUserId(2L);
    }

    @Test
    void unreadNotification_NotificationBelongsToUser_MarksNotViewedAndSendsUnreadCount() {
        when(notificationRepo.markNotificationAsNotViewedByIdAndTargetUserId(1L, 1L)).thenReturn(1);
        when(notificationRepo.countUnreadActionUsersByTargetUserId(1L)).thenReturn(1L);

        userNotificationService.unreadNotification(1L, 1L);

        verify(notificationRepo).markNotificationAsNotViewedByIdAndTargetUserId(1L, 1L);
        verify(messagingTemplate).convertAndSend("/topic/1/notification", 1L);
    }

    @Test
    void unreadNotification_NotificationMissing_ThrowsNotFoundException() {
        when(notificationRepo.markNotificationAsNotViewedByIdAndTargetUserId(1L, 1L)).thenReturn(0);

        assertThrows(NotFoundException.class, () -> userNotificationService.unreadNotification(1L, 1L));
        verify(notificationRepo, never()).countUnreadActionUsersByTargetUserId(1L);
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Long.class));
    }

    @Test
    void unreadNotification_NotificationDoesNotBelongToUser_ThrowsNotFoundException() {
        when(notificationRepo.markNotificationAsNotViewedByIdAndTargetUserId(1L, 2L)).thenReturn(0);

        assertThrows(NotFoundException.class, () -> userNotificationService.unreadNotification(2L, 1L));
        verify(notificationRepo, never()).countUnreadActionUsersByTargetUserId(2L);
    }

    @Test
    void countUnreadNotifications_ReturnsCountFromRepo() {
        when(notificationRepo.countUnreadActionUsersByTargetUserId(1L)).thenReturn(5L);

        long result = userNotificationService.countUnreadNotifications(1L);

        assertEquals(5L, result);
    }
}