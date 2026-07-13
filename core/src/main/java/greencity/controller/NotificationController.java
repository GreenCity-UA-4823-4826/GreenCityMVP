package greencity.controller;

import greencity.annotations.ApiPageableWithoutSort;
import greencity.annotations.CurrentUserId;
import greencity.annotations.ValidLanguage;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.notification.ActionDto;
import greencity.dto.notification.NotificationDto;
import greencity.enums.NotificationType;
import greencity.enums.ProjectName;
import greencity.service.UserNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final UserNotificationService userNotificationService;

    @Operation(summary = "Get page of notification filtered and sorted.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    })
    @ApiPageableWithoutSort
    @GetMapping
    public ResponseEntity<PageableAdvancedDto<NotificationDto>> getNotificationsFiltered(
        @Parameter(hidden = true) Pageable pageable,
        @Parameter(hidden = true) @CurrentUserId Long userId,
        @Parameter(hidden = true) @ValidLanguage Locale locale,
        @RequestParam(name = "project-name", required = false) ProjectName projectName,
        @RequestParam(name = "notification-types", required = false) List<NotificationType> notificationTypes,
        @RequestParam(required = false) Boolean viewed) {
        return ResponseEntity.ok().body(userNotificationService.getNotificationsFiltered(userId, pageable,
            locale.getLanguage(), projectName, notificationTypes, viewed));
    }

    @Operation(summary = "Mark Notification as viewed.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/{notificationId}/viewNotification")
    public ResponseEntity<Object> viewNotification(@PathVariable Long notificationId) {
        userNotificationService.viewNotification(notificationId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Mark Notification as unread.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/{notificationId}/unreadNotification")
    public ResponseEntity<Object> unreadNotification(@PathVariable Long notificationId) {
        userNotificationService.unreadNotification(notificationId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete single Notification.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Object> deleteNotification(
        @Parameter(hidden = true) @CurrentUserId Long userId,
        @PathVariable Long notificationId) {
        userNotificationService.deleteNotification(userId, notificationId);
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/notifications")
    public void notificationSocket(@Payload ActionDto user) {
        userNotificationService.notificationSocket(user);
    }
}
