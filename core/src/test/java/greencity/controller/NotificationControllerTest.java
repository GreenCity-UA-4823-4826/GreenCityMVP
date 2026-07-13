package greencity.controller;

import greencity.ModelUtils;
import greencity.converters.UserIdArgumentResolver;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.notification.NotificationDto;
import greencity.dto.user.UserVO;
import greencity.enums.NotificationType;
import greencity.enums.ProjectName;
import greencity.service.UserNotificationService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import java.security.Principal;
import java.util.List;

import static greencity.ModelUtils.getPrincipal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {
    private static final String notificationLink = "/notifications";
    private MockMvc mockMvc;

    @InjectMocks
    private NotificationController notificationController;

    @Mock
    private UserNotificationService userNotificationService;

    @Mock
    private UserService userService;

    @Mock
    private Validator mockValidator;

    private final Principal principal = getPrincipal();
    private final UserVO userVO = ModelUtils.getUserVO();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserIdArgumentResolver(userService))
            .setValidator(mockValidator)
            .build();
    }

    @Test
    void getNotificationsFiltered_ValidParams_ReturnsOk() throws Exception {
        PageableAdvancedDto<NotificationDto> responseDto = new PageableAdvancedDto<>(
            List.of(NotificationDto.builder().notificationId(1L).build()), 1L, 0, 1, 0, false, false, true, true);

        when(userNotificationService.getNotificationsFiltered(eq(1L), any(Pageable.class), eq("en"),
            eq(ProjectName.GREENCITY), eq(List.of(NotificationType.ECONEWS_COMMENT)), eq(false)))
            .thenReturn(responseDto);
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(get(notificationLink)
            .principal(principal)
            .param("project-name", "GREENCITY")
            .param("notification-types", "ECONEWS_COMMENT")
            .param("viewed", "false")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(userNotificationService).getNotificationsFiltered(eq(1L), any(Pageable.class), eq("en"),
            eq(ProjectName.GREENCITY), eq(List.of(NotificationType.ECONEWS_COMMENT)), eq(false));
    }

    @Test
    void viewNotification_ValidId_ReturnsOk() throws Exception {
        mockMvc.perform(post(notificationLink + "/{notificationId}/viewNotification", 1L)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(userNotificationService).viewNotification(1L);
    }

    @Test
    void unreadNotification_ValidId_ReturnsOk() throws Exception {
        mockMvc.perform(post(notificationLink + "/{notificationId}/unreadNotification", 1L)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(userNotificationService).unreadNotification(1L);
    }

    @Test
    void deleteNotification_ValidId_ReturnsOk() throws Exception {
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(delete(notificationLink + "/{notificationId}", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(userNotificationService).deleteNotification(1L, 1L);
    }
}
