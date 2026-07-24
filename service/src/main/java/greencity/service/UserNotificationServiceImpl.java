package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.notification.ActionDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.user.UserVO;
import greencity.entity.Notification;
import greencity.entity.Notification_;
import greencity.entity.User;
import greencity.enums.NotificationType;
import greencity.enums.ProjectName;
import greencity.exception.exceptions.NotFoundException;
import greencity.filters.NotificationSpecification;
import greencity.filters.SearchCriteria;
import greencity.repository.NotificationRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import org.apache.commons.lang3.StringUtils;
import static greencity.constant.AppConstant.FIRST_USER_PLACEHOLDER;
import static greencity.constant.AppConstant.SECOND_MESSAGE_PLACEHOLDER;
import static greencity.constant.AppConstant.SECOND_USER_PLACEHOLDER;
import static greencity.constant.AppConstant.THREE_OR_MORE_USERS;
import static greencity.constant.AppConstant.TWO_USERS;
import static greencity.constant.AppConstant.USER_PLACEHOLDER;

@Service
@RequiredArgsConstructor
@Transactional
public class UserNotificationServiceImpl implements UserNotificationService {
    private static final String TOPIC = "/topic/";
    private static final String NOTIFICATION = "/notification";

    private final NotificationRepo notificationRepo;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public PageableAdvancedDto<NotificationDto> getNotificationsFiltered(Long userId, Pageable pageable,
        String language, ProjectName projectName, List<NotificationType> notificationTypes, Boolean viewed) {
        List<SearchCriteria> criteriaList = new ArrayList<>();
        setValueIfNotEmpty(criteriaList, Notification_.TARGET_USER, userId);
        setValueIfNotEmpty(criteriaList, Notification_.PROJECT_NAME, projectName);
        if (notificationTypes != null && !notificationTypes.isEmpty()) {
            setValueIfNotEmpty(criteriaList, Notification_.NOTIFICATION_TYPE,
                notificationTypes.toArray(new NotificationType[0]));
        }
        if (viewed != null) {
            setValueIfNotEmpty(criteriaList, Notification_.VIEWED, viewed.toString());
        }
        Specification<Notification> specification = new NotificationSpecification(criteriaList);
        Page<Notification> notificationsPage = notificationRepo.findAll(specification, pageable);
        return buildPageableAdvancedDto(notificationsPage, language);
    }

    @Override
    public void notificationSocket(ActionDto user) {
        Long count = notificationRepo.countByTargetUserIdAndViewedIsFalse(user.getUserId());
        messagingTemplate.convertAndSend(TOPIC + user.getUserId() + NOTIFICATION, count);
    }

    @Override
    public void createNotification(UserVO targetUser, UserVO actionUser, NotificationType notificationType,
        Long targetId, String secondMessageText, Long secondMessageId) {
        Notification notification = findExistingNotification(targetUser.getId(), notificationType, targetId)
            .orElseGet(() -> buildNotification(notificationType, targetUser, targetId, secondMessageText, secondMessageId));
        updateNotificationWithActionUser(notification, actionUser, secondMessageText);
        saveAndNotify(notification);
    }

    @Override
    public void deleteNotification(Long userId, Long notificationId) {
        if (!notificationRepo.existsByIdAndTargetUserId(notificationId, userId)) {
            throw new NotFoundException(ErrorMessage.NOTIFICATION_NOT_FOUND_BY_ID + notificationId);
        }
        notificationRepo.deleteNotificationByIdAndTargetUserId(notificationId, userId);
    }

    @Override
    public void unreadNotification(Long notificationId) {
        Notification notification = notificationRepo.findById(notificationId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.NOTIFICATION_NOT_FOUND_BY_ID + notificationId));
        Long userId = notification.getTargetUser().getId();
        notificationRepo.markNotificationAsNotViewed(notificationId);
        sendNotificationCount(userId);
    }

    @Override
    public void viewNotification(Long notificationId) {
        Notification notification = notificationRepo.findById(notificationId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.NOTIFICATION_NOT_FOUND_BY_ID + notificationId));
        Long userId = notification.getTargetUser().getId();
        notificationRepo.markNotificationAsViewed(notificationId);
        sendNotificationCount(userId);
    }

    private PageableAdvancedDto<NotificationDto> buildPageableAdvancedDto(Page<Notification> notifications,
        String language) {
        List<NotificationDto> notificationDtoList = new LinkedList<>();
        for (Notification notification : notifications) {
            notificationDtoList.add(createNotificationDto(notification, language));
        }
        return new PageableAdvancedDto<>(
            notificationDtoList,
            notifications.getTotalElements(),
            notifications.getPageable().getPageNumber(),
            notifications.getTotalPages(),
            notifications.getNumber(),
            notifications.hasPrevious(),
            notifications.hasNext(),
            notifications.isFirst(),
            notifications.isLast());
    }

    private NotificationDto createNotificationDto(Notification notification, String language) {
        NotificationDto dto = modelMapper.map(notification, NotificationDto.class);
        ResourceBundle bundle = ResourceBundle.getBundle("notification", Locale.forLanguageTag(language),
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT));

        List<User> uniqueUsers = notification.getActionUsers().stream().distinct().toList();
        List<User> referencedUsers = uniqueUsers.subList(Math.max(0, uniqueUsers.size() - 2), uniqueUsers.size());

        dto.setTitleText(bundle.getString(dto.getNotificationType() + "_TITLE"));
        setActionUserDetails(dto, referencedUsers);
        dto.setBodyText(generateBodyText(notification, bundle, uniqueUsers.size(), referencedUsers));
        return dto;
    }

    private void sendNotificationCount(Long userId) {
        long count = notificationRepo.countByTargetUserIdAndViewedIsFalse(userId);
        messagingTemplate.convertAndSend(TOPIC + userId + NOTIFICATION, count);
    }

    private void saveAndNotify(Notification notification) {
        Notification savedNotification = notificationRepo.save(notification);
        sendNotificationCount(savedNotification.getTargetUser().getId());
    }

    private Notification buildNotification(NotificationType notificationType, UserVO targetUserVO, Long targetId,
        String secondMessageText, Long secondMessageId) {
        return Notification.builder()
            .notificationType(notificationType)
            .projectName(ProjectName.GREENCITY)
            .targetUser(modelMapper.map(targetUserVO, User.class))
            .actionUsers(new ArrayList<>())
            .targetId(targetId)
            .secondMessage(secondMessageText)
            .secondMessageId(secondMessageId)
            .time(ZonedDateTime.now())
            .emailSent(false)
            .build();
    }

    private Optional<Notification> findExistingNotification(Long userId, NotificationType notificationType,
        Long targetId) {
        return notificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse(
            userId, notificationType, targetId);
    }

    private void updateNotificationWithActionUser(Notification notification, UserVO actionUserVO,
        String secondMessageText) {
        Long actionUserId = actionUserVO.getId();
        notification.getActionUsers().removeIf(user -> user.getId().equals(actionUserId));
        notification.getActionUsers().add(modelMapper.map(actionUserVO, User.class));
        notification.setTime(ZonedDateTime.now());
        notification.setSecondMessage(secondMessageText);
    }

    private void setActionUserDetails(NotificationDto dto, List<User> referencedUsers) {
        dto.setActionUserText(referencedUsers.stream().map(User::getName).toList());
        dto.setActionUserId(referencedUsers.stream().map(User::getId).toList());
    }

    private String generateBodyText(Notification notification, ResourceBundle bundle, int uniqueUserCount,
        List<User> referencedUsers) {
        String bodyTextTemplate = bundle.getString(notification.getNotificationType().toString());
        String userText = switch (uniqueUserCount) {
            case 1 -> referencedUsers.getFirst().getName();
            case 2 -> bundle.getString(TWO_USERS)
                .replace(FIRST_USER_PLACEHOLDER, referencedUsers.get(0).getName())
                .replace(SECOND_USER_PLACEHOLDER, referencedUsers.get(1).getName());
            default -> bundle.getString(THREE_OR_MORE_USERS)
                .replace(FIRST_USER_PLACEHOLDER, referencedUsers.get(0).getName())
                .replace(SECOND_USER_PLACEHOLDER, referencedUsers.get(1).getName());
        };
        return bodyTextTemplate
            .replace(USER_PLACEHOLDER, userText)
            .replace(SECOND_MESSAGE_PLACEHOLDER, notification.getSecondMessage());
    }

    private void setValueIfNotEmpty(List<SearchCriteria> searchCriteria, String key, Object value) {
        boolean isInvalid = value == null
            || (value instanceof String valueString && StringUtils.isEmpty(valueString.trim()));
        if (!isInvalid) {
            searchCriteria.add(SearchCriteria.builder()
                .key(key)
                .type(key)
                .value(value)
                .build());
        }
    }
}
