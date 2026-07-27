package greencity.controller;

import greencity.ModelUtils;
import greencity.constant.ErrorMessage;
import greencity.converters.UserArgumentResolver;
import greencity.dto.PageableDto;
import greencity.dto.event.EventPreviewResponseDto;
import greencity.dto.event.EventResponseDto;
import greencity.dto.event.EventSearchSuggestionResponseDto;
import greencity.dto.user.UserVO;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
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
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

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
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(eventController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new UserArgumentResolver(userService, modelMapper))
                .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        when(errorAttributes.getErrorAttributes(any(), any(ErrorAttributeOptions.class)))
            .thenReturn(new HashMap<>(Map.of(
                "path", "/events",
                "message", "error",
                "timestamp", java.time.LocalDateTime.now(),
                "trace", "")));
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

    @Test
    void deleteEvent_validRequest_returnsOk() throws Exception {
        UserVO userVO = ModelUtils.getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(delete(EVENTS_LINK + "/{eventId}", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(eventService, times(1)).deleteEvent(1L, userVO);
    }

    @Test
    void deleteEvent_eventNotFound_returnsNotFound() throws Exception {
        UserVO userVO = ModelUtils.getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        doThrow(new NotFoundException(ErrorMessage.EVENT_NOT_FOUND_BY_ID + 999L))
            .when(eventService).deleteEvent(999L, userVO);

        mockMvc.perform(delete(EVENTS_LINK + "/{eventId}", 999L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteEvent_noPermission_returnsForbidden() throws Exception {
        UserVO userVO = ModelUtils.getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        doThrow(new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION))
            .when(eventService).deleteEvent(1L, userVO);

        mockMvc.perform(delete(EVENTS_LINK + "/{eventId}", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void getMyEvents_validRequest_returnsOkWithPage() throws Exception {
        UserVO userVO = ModelUtils.getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(eventService.getMyEvents(any(), any()))
            .thenReturn(new PageableDto<>(List.of(), 0, 0, 0));

        mockMvc.perform(get(EVENTS_LINK + "/myEvents")
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").isArray())
            .andExpect(jsonPath("$.totalElements").value(0))
            .andExpect(jsonPath("$.currentPage").value(0))
            .andExpect(jsonPath("$.totalPages").value(0));

        verify(eventService, times(1)).getMyEvents(any(), any());
    }

    @Test
    void getMyEvents_userNotFound_returnsNotFound() throws Exception {
        UserVO userVO = ModelUtils.getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        when(eventService.getMyEvents(any(), any()))
            .thenThrow(new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + userVO.getId()));

        mockMvc.perform(get(EVENTS_LINK + "/myEvents")
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateEvent_validRequest_returnsOkWithBody() throws Exception {
        UserVO userVO = ModelUtils.getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        String eventJson = "{"
            + "\"title\":\"Updated park cleanup\","
            + "\"description\":\"Updated description that is long enough to pass validation\","
            + "\"dates\":[{\"date\":\"2026-12-01\",\"startTime\":\"10:00:00\","
            + "\"endTime\":\"14:00:00\",\"allDay\":false}],"
            + "\"eventTypes\":[\"PLACE\"],"
            + "\"initiativeTypes\":[\"ENVIRONMENTAL\"],"
            + "\"visibility\":\"OPEN\","
            + "\"location\":\"Shevchenko Park\","
            + "\"latitude\":49.83,"
            + "\"longitude\":24.02,"
            + "\"imageOrder\":[]"
            + "}";

        MockMultipartFile eventPart = new MockMultipartFile(
            "eventUpdateRequestDto", "", "application/json", eventJson.getBytes());

        EventResponseDto responseDto = EventResponseDto.builder()
            .id(1L)
            .title("Updated park cleanup")
            .organizerId(userVO.getId())
            .build();

        when(eventService.updateEvent(any(), any(), any(), any())).thenReturn(responseDto);

        mockMvc.perform(multipart(HttpMethod.PUT, EVENTS_LINK + "/{eventId}", 1L)
            .file(eventPart)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(responseDto.getId()))
            .andExpect(jsonPath("$.title").value(responseDto.getTitle()))
            .andExpect(jsonPath("$.organizerId").value(responseDto.getOrganizerId()));

        verify(eventService, times(1)).updateEvent(any(), any(), any(), any());
    }

    @Test
    void updateEvent_eventNotFound_returnsNotFound() throws Exception {
        UserVO userVO = ModelUtils.getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        String eventJson = "{"
            + "\"title\":\"Updated park cleanup\","
            + "\"description\":\"Updated description that is long enough to pass validation\","
            + "\"dates\":[{\"date\":\"2026-12-01\",\"startTime\":\"10:00:00\","
            + "\"endTime\":\"14:00:00\",\"allDay\":false}],"
            + "\"eventTypes\":[\"PLACE\"],"
            + "\"initiativeTypes\":[\"ENVIRONMENTAL\"],"
            + "\"visibility\":\"OPEN\","
            + "\"location\":\"Shevchenko Park\","
            + "\"latitude\":49.83,"
            + "\"longitude\":24.02,"
            + "\"imageOrder\":[]"
            + "}";

        MockMultipartFile eventPart = new MockMultipartFile(
            "eventUpdateRequestDto", "", "application/json", eventJson.getBytes());

        when(eventService.updateEvent(any(), any(), any(), any()))
            .thenThrow(new NotFoundException(ErrorMessage.EVENT_NOT_FOUND_BY_ID + 999L));

        mockMvc.perform(multipart(HttpMethod.PUT, EVENTS_LINK + "/{eventId}", 999L)
            .file(eventPart)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateEvent_noPermission_returnsForbidden() throws Exception {
        UserVO userVO = ModelUtils.getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        String eventJson = "{"
            + "\"title\":\"Updated park cleanup\","
            + "\"description\":\"Updated description that is long enough to pass validation\","
            + "\"dates\":[{\"date\":\"2026-12-01\",\"startTime\":\"10:00:00\","
            + "\"endTime\":\"14:00:00\",\"allDay\":false}],"
            + "\"eventTypes\":[\"PLACE\"],"
            + "\"initiativeTypes\":[\"ENVIRONMENTAL\"],"
            + "\"visibility\":\"OPEN\","
            + "\"location\":\"Shevchenko Park\","
            + "\"latitude\":49.83,"
            + "\"longitude\":24.02,"
            + "\"imageOrder\":[]"
            + "}";

        MockMultipartFile eventPart = new MockMultipartFile(
            "eventUpdateRequestDto", "", "application/json", eventJson.getBytes());

        when(eventService.updateEvent(any(), any(), any(), any()))
            .thenThrow(new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION));

        mockMvc.perform(multipart(HttpMethod.PUT, EVENTS_LINK + "/{eventId}", 1L)
            .file(eventPart)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void updateEvent_pastEvent_returnsBadRequest() throws Exception {
        UserVO userVO = ModelUtils.getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        String eventJson = "{"
            + "\"title\":\"Updated park cleanup\","
            + "\"description\":\"Updated description that is long enough to pass validation\","
            + "\"dates\":[{\"date\":\"2026-12-01\",\"startTime\":\"10:00:00\","
            + "\"endTime\":\"14:00:00\",\"allDay\":false}],"
            + "\"eventTypes\":[\"PLACE\"],"
            + "\"initiativeTypes\":[\"ENVIRONMENTAL\"],"
            + "\"visibility\":\"OPEN\","
            + "\"location\":\"Shevchenko Park\","
            + "\"latitude\":49.83,"
            + "\"longitude\":24.02,"
            + "\"imageOrder\":[]"
            + "}";

        MockMultipartFile eventPart = new MockMultipartFile(
            "eventUpdateRequestDto", "", "application/json", eventJson.getBytes());

        when(eventService.updateEvent(any(), any(), any(), any()))
            .thenThrow(new BadRequestException(ErrorMessage.EVENT_ALREADY_PASSED));

        mockMvc.perform(multipart(HttpMethod.PUT, EVENTS_LINK + "/{eventId}", 1L)
            .file(eventPart)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateEvent_blankTitle_returnsBadRequest() throws Exception {
        UserVO userVO = ModelUtils.getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        String eventJson = "{"
            + "\"title\":\"\","
            + "\"description\":\"Updated description that is long enough to pass validation\","
            + "\"dates\":[{\"date\":\"2026-12-01\",\"startTime\":\"10:00:00\","
            + "\"endTime\":\"14:00:00\",\"allDay\":false}],"
            + "\"eventTypes\":[\"PLACE\"],"
            + "\"initiativeTypes\":[\"ENVIRONMENTAL\"],"
            + "\"location\":\"Shevchenko Park\","
            + "\"imageOrder\":[]"
            + "}";

        MockMultipartFile eventPart = new MockMultipartFile(
            "eventUpdateRequestDto", "", "application/json", eventJson.getBytes());

        mockMvc.perform(multipart(HttpMethod.PUT, EVENTS_LINK + "/{eventId}", 1L)
            .file(eventPart)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(eventService);
    }

    @Test
    void getSearchSuggestions_validQuery_returnsOkWithList() throws Exception {
        List<EventSearchSuggestionResponseDto> suggestions = List.of(
                EventSearchSuggestionResponseDto.builder().id(1L).title("Summer Fest").build(),
                EventSearchSuggestionResponseDto.builder().id(2L).title("Summer Run").build()
        );

        when(eventService.getSearchSuggestions("Summer")).thenReturn(suggestions);

        mockMvc.perform(get(EVENTS_LINK + "/search/suggestions")
                        .param("query", "Summer")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Summer Fest"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("Summer Run"));

        verify(eventService, times(1)).getSearchSuggestions("Summer");
    }

    @Test
    void getSearchSuggestions_queryTooLong_returnsBadRequest() throws Exception {
        String longQuery = "a".repeat(65);

        when(eventService.getSearchSuggestions(longQuery))
                .thenThrow(new BadRequestException(ErrorMessage.SEARCH_QUERY_TOO_LONG));

        mockMvc.perform(get(EVENTS_LINK + "/search/suggestions")
                        .param("query", longQuery)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(eventService, times(1)).getSearchSuggestions(longQuery);
    }

    @Test
    void getSearchSuggestions_noResults_returnsOkWithEmptyList() throws Exception {
        when(eventService.getSearchSuggestions("xyz")).thenReturn(List.of());

        mockMvc.perform(get(EVENTS_LINK + "/search/suggestions")
                        .param("query", "xyz")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(eventService, times(1)).getSearchSuggestions("xyz");
    }

    @Test
    void searchEvents_validQuery_returnsOkWithList() throws Exception {
        List<EventPreviewResponseDto> results = List.of(
                EventPreviewResponseDto.builder().id(1L).title("Summer Fest").build(),
                EventPreviewResponseDto.builder().id(2L).title("Summer Run").build()
        );
        Page<EventPreviewResponseDto> page = new PageImpl<>(results, PageRequest.of(0, 10), results.size());

        when(eventService.searchEvents(anyString(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get(EVENTS_LINK + "/search")
                        .param("query", "Summer")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Summer Fest"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].title").value("Summer Run"));

        verify(eventService, times(1)).searchEvents(anyString(), any(Pageable.class));
    }

    @Test
    void searchEvents_queryTooLong_returnsBadRequest() throws Exception {
        String longQuery = "a".repeat(65);

        when(eventService.searchEvents(anyString(), any(Pageable.class)))
                .thenThrow(new BadRequestException(ErrorMessage.SEARCH_QUERY_TOO_LONG));

        mockMvc.perform(get(EVENTS_LINK + "/search")
                        .param("query", longQuery)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(eventService, times(1)).searchEvents(anyString(), any(Pageable.class));
    }

    @Test
    void searchEvents_noResults_returnsOkWithEmptyList() throws Exception {
        Page<EventPreviewResponseDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(eventService.searchEvents(anyString(), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get(EVENTS_LINK + "/search")
                        .param("query", "xyz")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());

        verify(eventService, times(1)).searchEvents(anyString(), any(Pageable.class));
    }
}