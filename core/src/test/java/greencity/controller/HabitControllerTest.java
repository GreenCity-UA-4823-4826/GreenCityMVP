package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.HabitService;
import greencity.service.TagsService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;
import java.util.Locale;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HabitControllerTest {

    private static final String HABIT_LINK = "/habit";
    private static final Locale LOCALE = new Locale("en");

    private MockMvc mockMvc;

    @InjectMocks
    private HabitController habitController;

    @Mock
    private HabitService habitService;

    @Mock
    private TagsService tagsService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ObjectMapper objectMapper;

    private final Principal principal = getPrincipal();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(habitController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new UserArgumentResolver(userService, modelMapper))
                .setControllerAdvice(new CustomExceptionHandler(new DefaultErrorAttributes(), objectMapper))
                .build();
    }

    @Test
    void getHabitByIdTest() throws Exception {
        mockMvc.perform(get(HABIT_LINK + "/{id}", 1L)
                        .param("lang", LOCALE.getLanguage()))
                .andExpect(status().isOk());

        verify(habitService).getByIdAndLanguageCode(1L, LOCALE.getLanguage());
    }

    @Test
    void getAllHabitsTest() throws Exception {
        when(userService.findByEmail(any())).thenReturn(getUserVO());

        mockMvc.perform(get(HABIT_LINK)
                        .param("lang", LOCALE.getLanguage())
                        .principal(principal))
                .andExpect(status().isOk());

        verify(habitService).getAllHabitsByLanguageCode(any(), any(Pageable.class), eq(LOCALE.getLanguage()));
    }

    @Test
    void getShoppingListItemsTest() throws Exception {
        mockMvc.perform(get(HABIT_LINK + "/{id}/shopping-list", 1L)
                        .param("lang", LOCALE.getLanguage()))
                .andExpect(status().isOk());

        verify(habitService).getShoppingListForHabit(1L, LOCALE.getLanguage());
    }

    @Test
    void getAllByTagsAndLanguageCodeTest() throws Exception {
        List<String> tags = List.of("eco", "health");

        mockMvc.perform(get(HABIT_LINK + "/tags/search")
                        .param("lang", LOCALE.getLanguage())
                        .param("tags", "eco", "health"))
                .andExpect(status().isOk());

        verify(habitService).getAllByTagsAndLanguageCode(any(Pageable.class), eq(tags), eq(LOCALE.getLanguage()));
    }

    @Test
    void getAllByDifferentParametersTest() throws Exception {
        when(userService.findByEmail(any())).thenReturn(getUserVO());

        mockMvc.perform(get(HABIT_LINK + "/search")
                        .param("lang", LOCALE.getLanguage())
                        .param("tags", "eco")
                        .principal(principal))
                .andExpect(status().isOk());

        verify(habitService).getAllByDifferentParameters(
                any(), any(Pageable.class), any(), any(), any(), eq(LOCALE.getLanguage()));
    }

    @Test
    void getAllByDifferentParametersBadRequestTest() throws Exception {
        when(userService.findByEmail(any())).thenReturn(getUserVO());

        mockMvc.perform(get(HABIT_LINK + "/search")
                        .param("lang", LOCALE.getLanguage())
                        .principal(principal))
                .andExpect(status().isBadRequest());

        verify(habitService, never()).getAllByDifferentParameters(any(), any(), any(), any(), any(), any());
    }

    @Test
    void findAllHabitsTagsTest() throws Exception {
        mockMvc.perform(get(HABIT_LINK + "/tags")
                        .param("lang", LOCALE.getLanguage()))
                .andExpect(status().isOk());

        verify(tagsService).findAllHabitsTags(LOCALE.getLanguage());
    }

    @Test
    void addCustomHabitTest() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@gmail.com");

        String json = new ObjectMapper().writeValueAsString(
                greencity.ModelUtils.getAddCustomHabitDtoRequest());
        MockMultipartFile jsonFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes());
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "habit.jpg", MediaType.IMAGE_JPEG_VALUE, "image".getBytes());

        mockMvc.perform(multipart(HABIT_LINK + "/custom")
                        .file(jsonFile)
                        .file(imageFile)
                        .principal(mockPrincipal))
                .andExpect(status().isCreated());

        verify(habitService).addCustomHabit(any(), any(), eq("test@gmail.com"));
    }

    @Test
    void getFriendsAssignedToHabitProfilePicturesTest() throws Exception {
        when(userService.findByEmail(any())).thenReturn(getUserVO());

        mockMvc.perform(get(HABIT_LINK + "/{habitId}/friends/profile-pictures", 1L)
                        .principal(principal))
                .andExpect(status().isOk());

        verify(habitService).getFriendsAssignedToHabitProfilePictures(1L, getUserVO().getId());
    }
}
