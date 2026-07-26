package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
import greencity.dto.PageableDto;
import greencity.dto.habitinvite.HabitInviteDto;
import greencity.dto.habitinvite.SendHabitInviteDtoRequest;
import greencity.dto.user.UserVO;
import greencity.enums.HabitInviteStatus;
import greencity.service.HabitInviteService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HabitInviteControllerTest {
    private static final String habitInviteControllerLink = "/habit/invites";
    private MockMvc mockMvc;

    @InjectMocks
    private HabitInviteController habitInviteController;

    @Mock
    private HabitInviteService habitInviteService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    private final Principal principal = getPrincipal();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(habitInviteController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .build();
    }

    private HabitInviteDto buildDto(HabitInviteStatus status) {
        return HabitInviteDto.builder().id(1L).habitId(1L).inviterId(1L).inviteeId(2L).status(status).build();
    }

    @Test
    void sendInvite_ValidRequest_ReturnsCreated() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitInviteService.sendInvite(eq(1L), eq(2L), eq(userVO))).thenReturn(buildDto(HabitInviteStatus.PENDING));
        String content = "{\"inviteeId\": 2}";

        mockMvc.perform(post(habitInviteControllerLink + "/{habitId}", 1L)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content))
            .andExpect(status().isCreated());

        ObjectMapper mapper = new ObjectMapper();
        SendHabitInviteDtoRequest request = mapper.readValue(content, SendHabitInviteDtoRequest.class);
        verify(habitInviteService).sendInvite(1L, request.getInviteeId(), userVO);
    }

    @Test
    void cancelInvite_ValidRequest_ReturnsOk() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitInviteService.cancelInvite(1L, userVO)).thenReturn(buildDto(HabitInviteStatus.CANCELLED));

        mockMvc.perform(post(habitInviteControllerLink + "/{inviteId}/cancel", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(habitInviteService).cancelInvite(1L, userVO);
    }

    @Test
    void acceptInvite_ValidRequest_ReturnsOk() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitInviteService.acceptInvite(1L, userVO)).thenReturn(buildDto(HabitInviteStatus.ACCEPTED));

        mockMvc.perform(post(habitInviteControllerLink + "/{inviteId}/accept", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(habitInviteService).acceptInvite(1L, userVO);
    }

    @Test
    void declineInvite_ValidRequest_ReturnsOk() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(habitInviteService.declineInvite(1L, userVO)).thenReturn(buildDto(HabitInviteStatus.DECLINED));

        mockMvc.perform(post(habitInviteControllerLink + "/{inviteId}/decline", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(habitInviteService).declineInvite(1L, userVO);
    }

    @Test
    void getSentPendingInvites_ValidRequest_ReturnsOk() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        PageableDto<HabitInviteDto> pageableDto = new PageableDto<>(
            List.of(buildDto(HabitInviteStatus.PENDING)), 1, 0, 1);
        when(habitInviteService.getSentPendingInvites(eq(userVO), any(Pageable.class))).thenReturn(pageableDto);

        mockMvc.perform(get(habitInviteControllerLink + "/sent")
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(habitInviteService).getSentPendingInvites(eq(userVO), any(Pageable.class));
    }
}
