package greencity.controller;

import greencity.converters.UserArgumentResolver;
import greencity.dto.eventlike.EventLikeDtoResponse;
import greencity.dto.user.UserVO;
import greencity.service.EventLikeService;
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
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventLikeControllerTest {
    private static final String eventLikeControllerLink = "/events/likes";
    private MockMvc mockMvc;

    @InjectMocks
    private EventLikeController eventLikeController;

    @Mock
    private EventLikeService eventLikeService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    private final Principal principal = getPrincipal();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(eventLikeController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .build();
    }

    @Test
    void like_ValidRequest_ReturnsCreated() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(eventLikeService.like(eq(1L), eq(userVO)))
            .thenReturn(EventLikeDtoResponse.builder().eventId(1L).likesCount(1L).liked(true).build());

        mockMvc.perform(post(eventLikeControllerLink + "/{eventId}", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        verify(eventLikeService).like(1L, userVO);
    }

    @Test
    void unlike_ValidRequest_ReturnsOk() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(eventLikeService.unlike(eq(1L), eq(userVO)))
            .thenReturn(EventLikeDtoResponse.builder().eventId(1L).likesCount(0L).liked(false).build());

        mockMvc.perform(delete(eventLikeControllerLink + "/{eventId}", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(eventLikeService).unlike(1L, userVO);
    }
}
