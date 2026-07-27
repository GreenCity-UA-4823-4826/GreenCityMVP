package greencity.repository;

import greencity.entity.Notification;
import greencity.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.viewed = true WHERE n.id = :notificationId "
        + "AND n.targetUser.id = :targetUserId")
    int markNotificationAsViewedByIdAndTargetUserId(Long notificationId, Long targetUserId);

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.viewed = false WHERE n.id = :notificationId "
        + "AND n.targetUser.id = :targetUserId")
    int markNotificationAsNotViewedByIdAndTargetUserId(Long notificationId, Long targetUserId);

    Optional<Notification> findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(
        Long targetUserId, NotificationType notificationType, Long targetId);

    void deleteNotificationByIdAndTargetUserId(Long notificationId, Long targetUserId);

    @Query("SELECT COUNT(u) FROM Notification n JOIN n.actionUsers u "
        + "WHERE n.targetUser.id = :userId AND n.viewed = false")
    long countUnreadActionUsersByTargetUserId(Long userId);

    boolean existsByIdAndTargetUserId(Long notificationId, Long targetUserId);
}
