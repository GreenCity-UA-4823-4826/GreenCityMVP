package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
import greencity.dto.PageableDto;
import greencity.dto.eventcomment.AddEventCommentDtoRequest;
import greencity.dto.eventcomment.AddEventCommentDtoResponse;
import greencity.dto.eventcomment.EventCommentDto;
import greencity.dto.user.UserVO;
import greencity.service.EventCommentService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventCommentControllerTest {
    private static final String eventCommentControllerLink = "/events/comments";
    private MockMvc mockMvc;

    @InjectMocks
    private EventCommentController eventCommentController;

    @Mock
    private EventCommentService eventCommentService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    private final Principal principal = getPrincipal();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(eventCommentController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .build();
    }

    @Test
    void save_ValidRequest_ReturnsCreated() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);
        when(eventCommentService.save(eq(1L), any(AddEventCommentDtoRequest.class), eq(userVO)))
            .thenReturn(AddEventCommentDtoResponse.builder().id(1L).text("Nice event!").build());
        String content = "{\"text\": \"Nice event!\"}";

        mockMvc.perform(post(eventCommentControllerLink + "/{eventId}", 1L)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content))
            .andExpect(status().isCreated());

        ObjectMapper mapper = new ObjectMapper();
        AddEventCommentDtoRequest request = mapper.readValue(content, AddEventCommentDtoRequest.class);
        verify(eventCommentService).save(1L, request, userVO);
    }

    @Test
    void findAllComments_ValidRequest_ReturnsOk() throws Exception {
        PageableDto<EventCommentDto> pageableDto = new PageableDto<>(
            List.of(EventCommentDto.builder().id(1L).text("Nice event!").build()), 1, 0, 1);
        when(eventCommentService.findAllComments(any(Pageable.class), eq(1L))).thenReturn(pageableDto);

        mockMvc.perform(get(eventCommentControllerLink + "/{eventId}", 1L)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(eventCommentService).findAllComments(any(Pageable.class), eq(1L));
    }

    @Test
    void delete_ValidRequest_ReturnsOk() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(modelMapper.map(userVO, UserVO.class)).thenReturn(userVO);

        mockMvc.perform(delete(eventCommentControllerLink + "/{eventCommentId}", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(eventCommentService).deleteById(1L, userVO);
    }
}
