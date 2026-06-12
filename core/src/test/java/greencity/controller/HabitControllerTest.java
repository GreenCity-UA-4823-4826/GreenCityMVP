package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
import greencity.dto.PageableDto;
import greencity.dto.habit.AddCustomHabitDtoRequest;
import greencity.dto.habit.AddCustomHabitDtoResponse;
import greencity.dto.habit.HabitDto;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.dto.shoppinglistitem.ShoppingListItemDto;
import greencity.dto.user.UserProfilePictureDto;
import greencity.dto.user.UserVO;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.HabitService;
import greencity.service.TagsService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static greencity.ModelUtils.getAddCustomHabitDtoRequest;
import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HabitControllerTest {

    private static final String HABIT_LINK = "/habit";
    private static final Locale LOCALE = new Locale("en");
    private static final String PRINCIPAL_EMAIL = "test@gmail.com";

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
    private final UserVO userVO = getUserVO();

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(habitController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new UserArgumentResolver(userService, modelMapper))
                .setControllerAdvice(new CustomExceptionHandler(new DefaultErrorAttributes(), objectMapper))
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void getHabitByIdTest() throws Exception {
        HabitDto responseDto = getHabitDto();

        when(habitService.getByIdAndLanguageCode(1L, LOCALE.getLanguage())).thenReturn(responseDto);

        mockMvc.perform(get(HABIT_LINK + "/{id}", 1L)
                        .param("lang", LOCALE.getLanguage())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.complexity").value(responseDto.getComplexity()))
                .andExpect(jsonPath("$.habitTranslation.name").value(responseDto.getHabitTranslation().getName()));

        verify(habitService).getByIdAndLanguageCode(1L, LOCALE.getLanguage());
    }

    @Test
    void getAllHabitsTest() throws Exception {
        PageableDto<HabitDto> pageableDto = getPageableHabitDto();

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitService.getAllHabitsByLanguageCode(eq(userVO), any(Pageable.class), eq(LOCALE.getLanguage())))
                .thenReturn(pageableDto);

        mockMvc.perform(get(HABIT_LINK)
                        .param("lang", LOCALE.getLanguage())
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page[0].id").value(pageableDto.getPage().getFirst().getId()))
                .andExpect(jsonPath("$.totalElements").value(pageableDto.getTotalElements()))
                .andExpect(jsonPath("$.currentPage").value(pageableDto.getCurrentPage()))
                .andExpect(jsonPath("$.totalPages").value(pageableDto.getTotalPages()));

        verify(habitService).getAllHabitsByLanguageCode(eq(userVO), any(Pageable.class), eq(LOCALE.getLanguage()));
    }

    @Test
    void getShoppingListItemsTest() throws Exception {
        ShoppingListItemDto shoppingListItem = ShoppingListItemDto.builder()
                .id(1L)
                .text("buy a shopper")
                .status("ACTIVE")
                .build();

        when(habitService.getShoppingListForHabit(1L, LOCALE.getLanguage()))
                .thenReturn(List.of(shoppingListItem));

        mockMvc.perform(get(HABIT_LINK + "/{id}/shopping-list", 1L)
                        .param("lang", LOCALE.getLanguage())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(shoppingListItem.getId()))
                .andExpect(jsonPath("$[0].text").value(shoppingListItem.getText()));

        verify(habitService).getShoppingListForHabit(1L, LOCALE.getLanguage());
    }

    @Test
    void getAllByTagsAndLanguageCodeTest() throws Exception {
        List<String> tags = List.of("eco", "health");
        PageableDto<HabitDto> pageableDto = getPageableHabitDto();

        when(habitService.getAllByTagsAndLanguageCode(any(Pageable.class), eq(tags), eq(LOCALE.getLanguage())))
                .thenReturn(pageableDto);

        mockMvc.perform(get(HABIT_LINK + "/tags/search")
                        .param("lang", LOCALE.getLanguage())
                        .param("tags", "eco", "health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page[0].id").value(pageableDto.getPage().getFirst().getId()))
                .andExpect(jsonPath("$.totalElements").value(pageableDto.getTotalElements()));

        verify(habitService).getAllByTagsAndLanguageCode(any(Pageable.class), eq(tags), eq(LOCALE.getLanguage()));
    }

    @Test
    void getAllByDifferentParametersWithTagsTest() throws Exception {
        PageableDto<HabitDto> pageableDto = getPageableHabitDto();
        Optional<List<String>> tags = Optional.of(List.of("eco"));

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitService.getAllByDifferentParameters(
                eq(userVO), any(Pageable.class), eq(tags), eq(Optional.empty()), eq(Optional.empty()),
                eq(LOCALE.getLanguage())))
                .thenReturn(pageableDto);

        mockMvc.perform(get(HABIT_LINK + "/search")
                        .param("lang", LOCALE.getLanguage())
                        .param("tags", "eco")
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page[0].id").value(pageableDto.getPage().getFirst().getId()));

        verify(habitService).getAllByDifferentParameters(
                eq(userVO), any(Pageable.class), eq(tags), eq(Optional.empty()), eq(Optional.empty()),
                eq(LOCALE.getLanguage()));
    }

    @Test
    void getAllByDifferentParametersWithIsCustomHabitTest() throws Exception {
        PageableDto<HabitDto> pageableDto = getPageableHabitDto();

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitService.getAllByDifferentParameters(
                eq(userVO), any(Pageable.class), eq(Optional.empty()), eq(Optional.of(true)), eq(Optional.empty()),
                eq(LOCALE.getLanguage())))
                .thenReturn(pageableDto);

        mockMvc.perform(get(HABIT_LINK + "/search")
                        .param("lang", LOCALE.getLanguage())
                        .param("isCustomHabit", "true")
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page[0].isCustomHabit").value(pageableDto.getPage().getFirst().getIsCustomHabit()));

        verify(habitService).getAllByDifferentParameters(
                eq(userVO), any(Pageable.class), eq(Optional.empty()), eq(Optional.of(true)), eq(Optional.empty()),
                eq(LOCALE.getLanguage()));
    }

    @Test
    void getAllByDifferentParametersWithComplexitiesTest() throws Exception {
        PageableDto<HabitDto> pageableDto = getPageableHabitDto();
        Optional<List<Integer>> complexities = Optional.of(List.of(1, 2));

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitService.getAllByDifferentParameters(
                eq(userVO), any(Pageable.class), eq(Optional.empty()), eq(Optional.empty()), eq(complexities),
                eq(LOCALE.getLanguage())))
                .thenReturn(pageableDto);

        mockMvc.perform(get(HABIT_LINK + "/search")
                        .param("lang", LOCALE.getLanguage())
                        .param("complexities", "1", "2")
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page[0].complexity").value(pageableDto.getPage().getFirst().getComplexity()));

        verify(habitService).getAllByDifferentParameters(
                eq(userVO), any(Pageable.class), eq(Optional.empty()), eq(Optional.empty()), eq(complexities),
                eq(LOCALE.getLanguage()));
    }

    @Test
    void getAllByDifferentParametersWithAllFiltersTest() throws Exception {
        PageableDto<HabitDto> pageableDto = getPageableHabitDto();
        Optional<List<String>> tags = Optional.of(List.of("eco"));
        Optional<List<Integer>> complexities = Optional.of(List.of(1));

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitService.getAllByDifferentParameters(
                eq(userVO), any(Pageable.class), eq(tags), eq(Optional.of(true)), eq(complexities),
                eq(LOCALE.getLanguage())))
                .thenReturn(pageableDto);

        mockMvc.perform(get(HABIT_LINK + "/search")
                        .param("lang", LOCALE.getLanguage())
                        .param("tags", "eco")
                        .param("isCustomHabit", "true")
                        .param("complexities", "1")
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page[0].id").value(pageableDto.getPage().getFirst().getId()))
                .andExpect(jsonPath("$.page[0].isCustomHabit").value(pageableDto.getPage().getFirst().getIsCustomHabit()))
                .andExpect(jsonPath("$.page[0].complexity").value(pageableDto.getPage().getFirst().getComplexity()));

        verify(habitService).getAllByDifferentParameters(
                eq(userVO), any(Pageable.class), eq(tags), eq(Optional.of(true)), eq(complexities),
                eq(LOCALE.getLanguage()));
    }

    @Test
    void getAllByDifferentParametersBadRequestTest() throws Exception {
        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);

        mockMvc.perform(get(HABIT_LINK + "/search")
                        .param("lang", LOCALE.getLanguage())
                        .principal(principal))
                .andExpect(status().isBadRequest());

        verify(habitService, never()).getAllByDifferentParameters(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    void findAllHabitsTagsTest() throws Exception {
        List<String> tags = List.of("eco", "health");

        when(tagsService.findAllHabitsTags(LOCALE.getLanguage())).thenReturn(tags);

        mockMvc.perform(get(HABIT_LINK + "/tags")
                        .param("lang", LOCALE.getLanguage())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0]").value(tags.getFirst()))
                .andExpect(jsonPath("$[1]").value(tags.get(1)));

        verify(tagsService).findAllHabitsTags(LOCALE.getLanguage());
    }

    @Test
    void addCustomHabitTest() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(PRINCIPAL_EMAIL);

        AddCustomHabitDtoRequest request = getAddCustomHabitDtoRequest();
        AddCustomHabitDtoResponse response = AddCustomHabitDtoResponse.builder()
                .id(10L)
                .userId(userVO.getId())
                .complexity(request.getComplexity())
                .defaultDuration(request.getDefaultDuration())
                .image("uploaded-image.jpg")
                .tagIds(request.getTagIds())
                .build();

        String json = new ObjectMapper().writeValueAsString(request);
        MockMultipartFile jsonFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes());
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "habit.jpg", MediaType.IMAGE_JPEG_VALUE, "image".getBytes());

        when(habitService.addCustomHabit(any(AddCustomHabitDtoRequest.class), any(MultipartFile.class),
                eq(PRINCIPAL_EMAIL)))
                .thenReturn(response);

        mockMvc.perform(multipart(HABIT_LINK + "/custom")
                        .file(jsonFile)
                        .file(imageFile)
                        .principal(mockPrincipal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.userId").value(response.getUserId()))
                .andExpect(jsonPath("$.complexity").value(response.getComplexity()))
                .andExpect(jsonPath("$.defaultDuration").value(response.getDefaultDuration()))
                .andExpect(jsonPath("$.image").value(response.getImage()));

        ArgumentCaptor<AddCustomHabitDtoRequest> requestCaptor =
                ArgumentCaptor.forClass(AddCustomHabitDtoRequest.class);
        ArgumentCaptor<MultipartFile> imageCaptor = ArgumentCaptor.forClass(MultipartFile.class);

        verify(habitService).addCustomHabit(requestCaptor.capture(), imageCaptor.capture(), eq(PRINCIPAL_EMAIL));
        assertEquals(request.getComplexity(), requestCaptor.getValue().getComplexity());
        assertEquals(request.getDefaultDuration(), requestCaptor.getValue().getDefaultDuration());
        assertEquals(request.getTagIds(), requestCaptor.getValue().getTagIds());
        assertEquals("habit.jpg", imageCaptor.getValue().getOriginalFilename());
    }

    @Test
    void addCustomHabitMissingRequestPartTest() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(PRINCIPAL_EMAIL);

        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "habit.jpg", MediaType.IMAGE_JPEG_VALUE, "image".getBytes());

        mockMvc.perform(multipart(HABIT_LINK + "/custom")
                        .file(imageFile)
                        .principal(mockPrincipal))
                .andExpect(status().isBadRequest());

        verify(habitService, never()).addCustomHabit(any(), any(), any());
    }

    @Test
    void addCustomHabitInvalidRequestValidationTest() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(PRINCIPAL_EMAIL);

        String invalidJson = "{\"complexity\": 5, \"defaultDuration\": 7, \"tagIds\": [20]}";
        MockMultipartFile jsonFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, invalidJson.getBytes());
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "habit.jpg", MediaType.IMAGE_JPEG_VALUE, "image".getBytes());

        mockMvc.perform(multipart(HABIT_LINK + "/custom")
                        .file(jsonFile)
                        .file(imageFile)
                        .principal(mockPrincipal))
                .andExpect(status().isBadRequest());

        verify(habitService, never()).addCustomHabit(any(), any(), any());
    }

    @Test
    void addCustomHabitWithoutImagePartTest() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(PRINCIPAL_EMAIL);

        AddCustomHabitDtoRequest request = getAddCustomHabitDtoRequest();
        AddCustomHabitDtoResponse response = AddCustomHabitDtoResponse.builder()
                .id(10L)
                .userId(userVO.getId())
                .complexity(request.getComplexity())
                .defaultDuration(request.getDefaultDuration())
                .image("default-image.jpg")
                .tagIds(request.getTagIds())
                .build();

        String json = new ObjectMapper().writeValueAsString(request);
        MockMultipartFile jsonFile = new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes());

        when(habitService.addCustomHabit(any(AddCustomHabitDtoRequest.class), any(), eq(PRINCIPAL_EMAIL)))
                .thenReturn(response);

        mockMvc.perform(multipart(HABIT_LINK + "/custom")
                        .file(jsonFile)
                        .principal(mockPrincipal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.image").value(response.getImage()));

        ArgumentCaptor<MultipartFile> imageCaptor = ArgumentCaptor.forClass(MultipartFile.class);
        verify(habitService).addCustomHabit(any(AddCustomHabitDtoRequest.class), imageCaptor.capture(),
                eq(PRINCIPAL_EMAIL));
        assertNull(imageCaptor.getValue());
    }

    @Test
    void getFriendsAssignedToHabitProfilePicturesTest() throws Exception {
        UserProfilePictureDto profilePicture = new UserProfilePictureDto(2L, "friend", "friend.jpg");

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitService.getFriendsAssignedToHabitProfilePictures(1L, userVO.getId()))
                .thenReturn(List.of(profilePicture));

        mockMvc.perform(get(HABIT_LINK + "/{habitId}/friends/profile-pictures", 1L)
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(profilePicture.getId()))
                .andExpect(jsonPath("$[0].name").value(profilePicture.getName()))
                .andExpect(jsonPath("$[0].profilePicturePath").value(profilePicture.getProfilePicturePath()));

        verify(habitService).getFriendsAssignedToHabitProfilePictures(1L, userVO.getId());
    }

    private HabitDto getHabitDto() {
        return HabitDto.builder()
                .id(1L)
                .complexity(2)
                .defaultDuration(7)
                .image("habit.jpg")
                .isCustomHabit(true)
                .habitTranslation(HabitTranslationDto.builder()
                        .name("use shopper")
                        .description("Description")
                        .habitItem("Item")
                        .languageCode(LOCALE.getLanguage())
                        .build())
                .build();
    }

    private PageableDto<HabitDto> getPageableHabitDto() {
        return new PageableDto<>(List.of(getHabitDto()), 1L, 0, 1);
    }
}
