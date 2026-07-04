package greencity.controller;

import greencity.ModelUtils;
import greencity.converters.UserArgumentResolver;
import greencity.dto.event.EventResponseDto;
import greencity.dto.user.UserVO;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.EventService;
import greencity.service.UserService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventControllerTest {

    private static final String EVENTS_LINK = "/events";

    @InjectMocks
    private EventController eventController;

    @Mock
    private EventService eventService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ErrorAttributes errorAttributes;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Principal principal = () -> "test@gmail.com";

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(eventController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new UserArgumentResolver(userService, modelMapper))
                .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
                .build();
    }

    @Test
    void createEvent_validRequest_returnsCreatedWithBody() throws Exception {
        UserVO userVO = ModelUtils.getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        String eventJson = "{"
                + "\"title\":\"Park cleanup\","
                + "\"description\":\"Join us for a park cleanup, it will be fun and useful\","
                + "\"dates\":[{\"date\":\"2026-12-01\",\"startTime\":\"10:00:00\","
                + "\"endTime\":\"14:00:00\",\"allDay\":false}],"
                + "\"eventTypes\":[\"PLACE\"],"
                + "\"initiativeTypes\":[\"ENVIRONMENTAL\"],"
                + "\"visibility\":\"OPEN\","
                + "\"location\":\"Shevchenko Park\","
                + "\"latitude\":49.83,"
                + "\"longitude\":24.02"
                + "}";

        MockMultipartFile eventPart = new MockMultipartFile(
                "eventCreateRequestDto", "", "application/json", eventJson.getBytes());

        EventResponseDto responseDto = EventResponseDto.builder()
                .id(1L)
                .title("Park cleanup")
                .organizerId(userVO.getId())
                .build();

        when(eventService.createEvent(any(), any(), any(), any())).thenReturn(responseDto);

        mockMvc.perform(multipart(EVENTS_LINK)
                        .file(eventPart)
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.title").value(responseDto.getTitle()))
                .andExpect(jsonPath("$.organizerId").value(responseDto.getOrganizerId()));

        verify(eventService, times(1)).createEvent(any(), any(), any(), any());
    }

    @Test
    void createEvent_blankTitle_returnsBadRequest() throws Exception {
        UserVO userVO = ModelUtils.getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        String eventJson = "{"
                + "\"title\":\"\","
                + "\"description\":\"Join us for a park cleanup, it will be fun and useful\","
                + "\"dates\":[{\"date\":\"2026-12-01\",\"startTime\":\"10:00:00\","
                + "\"endTime\":\"14:00:00\",\"allDay\":false}],"
                + "\"eventTypes\":[\"PLACE\"],"
                + "\"initiativeTypes\":[\"ENVIRONMENTAL\"],"
                + "\"location\":\"Shevchenko Park\""
                + "}";

        MockMultipartFile eventPart = new MockMultipartFile(
                "eventCreateRequestDto", "", "application/json", eventJson.getBytes());

        mockMvc.perform(multipart(EVENTS_LINK)
                        .file(eventPart)
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(eventService);
    }

    @Test
    void createEvent_placeWithoutLocation_returnsBadRequest() throws Exception {
        UserVO userVO = ModelUtils.getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        String eventJson = "{"
                + "\"title\":\"Park cleanup\","
                + "\"description\":\"Join us for a park cleanup, it will be fun and useful\","
                + "\"dates\":[{\"date\":\"2026-12-01\",\"startTime\":\"10:00:00\","
                + "\"endTime\":\"14:00:00\",\"allDay\":false}],"
                + "\"eventTypes\":[\"PLACE\"],"
                + "\"initiativeTypes\":[\"ENVIRONMENTAL\"]"
                + "}";

        MockMultipartFile eventPart = new MockMultipartFile(
                "eventCreateRequestDto", "", "application/json", eventJson.getBytes());

        mockMvc.perform(multipart(EVENTS_LINK)
                        .file(eventPart)
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(eventService);
    }
}