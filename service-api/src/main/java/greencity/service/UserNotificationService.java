package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.notification.ActionDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.user.UserVO;
import greencity.enums.NotificationType;
import greencity.enums.ProjectName;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface UserNotificationService {
    PageableAdvancedDto<NotificationDto> getNotificationsFiltered(Long userId, Pageable pageable, String language,
        ProjectName projectName, List<NotificationType> notificationTypes, Boolean viewed);

    void notificationSocket(ActionDto user);

    void createNotification(UserVO targetUser, UserVO actionUser, NotificationType notificationType,
        Long targetId, String secondMessageText);

    void removeActionUser(UserVO targetUser, UserVO actionUser, NotificationType notificationType, Long targetId);

    void deleteNotification(Long userId, Long notificationId);

    void unreadNotification(Long userId, Long notificationId);

    void viewNotification(Long userId, Long notificationId);

    void viewNotification(Long notificationId);

    void deleteNotification(NotificationType notificationType, Long targetUserId, Long targetId);
    long countUnreadNotifications(Long userId);
}
