package greencity.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import greencity.converters.UserArgumentResolver;
import greencity.dto.habit.HabitAssignCustomPropertiesDto;
import greencity.dto.habit.HabitAssignDto;
import greencity.dto.habit.HabitAssignManagementDto;
import greencity.dto.habit.HabitAssignStatDto;
import greencity.dto.habit.HabitAssignUserDurationDto;
import greencity.dto.habit.HabitDto;
import greencity.dto.habit.HabitsDateEnrollmentDto;
import greencity.dto.habit.UpdateUserShoppingListDto;
import greencity.dto.habit.UserShoppingAndCustomShoppingListsDto;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.dto.user.UserVO;
import greencity.enums.HabitAssignStatus;
import greencity.service.HabitAssignService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.security.Principal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static greencity.ModelUtils.getHabitAssignCustomPropertiesDto;
import static greencity.ModelUtils.getUpdateUserShoppingListDto;
import static greencity.ModelUtils.getUserShoppingAndCustomShoppingListsDto;
import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HabitAssignControllerTest {

    private static final String BASE_URL = "/habit/assign";
    private static final Locale LOCALE = Locale.ENGLISH;
    private static final String PRINCIPAL_EMAIL = "test@gmail.com";
    private static final LocalDate TEST_DATE = LocalDate.of(2026, 6, 15);

    private MockMvc mockMvc;

    @InjectMocks
    private HabitAssignController habitAssignController;

    @Mock
    private HabitAssignService habitAssignService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final Principal principal = getPrincipal();
    private final UserVO userVO = getUserVO();

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        this.mockMvc = MockMvcBuilders.standaloneSetup(habitAssignController)
            .setCustomArgumentResolvers(new UserArgumentResolver(userService, modelMapper))
            .setValidator(validator)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
    }

    @Test
    void assignDefault_ValidHabitId_ReturnsCreated() throws Exception {
        HabitAssignManagementDto response = getHabitAssignManagementDto();

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitAssignService.assignDefaultHabitForUser(1L, userVO)).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/{habitId}", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(response.getId()))
            .andExpect(jsonPath("$.habitId").value(response.getHabitId()))
            .andExpect(jsonPath("$.userId").value(response.getUserId()))
            .andExpect(jsonPath("$.status").value(response.getStatus().name()));

        verify(habitAssignService).assignDefaultHabitForUser(1L, userVO);
    }

    @Test
    void assignCustom_ValidRequest_ReturnsCreated() throws Exception {
        HabitAssignCustomPropertiesDto request = getHabitAssignCustomPropertiesDto();
        List<HabitAssignManagementDto> response = List.of(getHabitAssignManagementDto());

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitAssignService.assignCustomHabitForUser(eq(1L), eq(userVO), any(HabitAssignCustomPropertiesDto.class)))
            .thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/{habitId}/custom", 1L)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(response.getFirst().getId()))
            .andExpect(jsonPath("$[0].duration").value(response.getFirst().getDuration()));

        ArgumentCaptor<HabitAssignCustomPropertiesDto> captor =
            ArgumentCaptor.forClass(HabitAssignCustomPropertiesDto.class);
        verify(habitAssignService).assignCustomHabitForUser(eq(1L), eq(userVO), captor.capture());
        assertEquals(request.getFriendsIdsList(), captor.getValue().getFriendsIdsList());
        assertEquals(request.getHabitAssignPropertiesDto().getDuration(),
            captor.getValue().getHabitAssignPropertiesDto().getDuration());
    }

    @Test
    void updateHabitAssignDuration_ValidDuration_ReturnsOk() throws Exception {
        HabitAssignUserDurationDto response = HabitAssignUserDurationDto.builder()
            .habitAssignId(1L)
            .userId(userVO.getId())
            .habitId(2L)
            .status(HabitAssignStatus.INPROGRESS)
            .workingDays(5)
            .duration(14)
            .build();

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitAssignService.updateUserHabitInfoDuration(1L, userVO.getId(), 14)).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/{habitAssignId}/update-habit-duration", 1L)
            .principal(principal)
            .param("duration", "14")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.habitAssignId").value(response.getHabitAssignId()))
            .andExpect(jsonPath("$.duration").value(response.getDuration()))
            .andExpect(jsonPath("$.status").value(response.getStatus().name()));

        verify(habitAssignService).updateUserHabitInfoDuration(1L, userVO.getId(), 14);
    }

    @Test
    void getHabitAssign_ValidIdAndLocale_ReturnsOk() throws Exception {
        HabitAssignDto response = getHabitAssignDto();

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitAssignService.getByHabitAssignIdAndUserId(1L, userVO.getId(), LOCALE.getLanguage()))
            .thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{habitAssignId}", 1L)
            .principal(principal)
            .locale(LOCALE)
            .param("lang", LOCALE.getLanguage())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(response.getId()))
            .andExpect(jsonPath("$.userId").value(response.getUserId()))
            .andExpect(jsonPath("$.status").value(response.getStatus().name()));

        verify(habitAssignService).getByHabitAssignIdAndUserId(1L, userVO.getId(), LOCALE.getLanguage());
    }

    @Test
    void getCurrentUserHabitAssignsByIdAndAcquired_ValidLocale_ReturnsOk() throws Exception {
        List<HabitAssignDto> response = List.of(getHabitAssignDto());

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitAssignService.getAllHabitAssignsByUserIdAndStatusNotCancelled(userVO.getId(), LOCALE.getLanguage()))
            .thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/allForCurrentUser")
            .principal(principal)
            .locale(LOCALE)
            .param("lang", LOCALE.getLanguage())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(response.getFirst().getId()))
            .andExpect(jsonPath("$[0].duration").value(response.getFirst().getDuration()));

        verify(habitAssignService).getAllHabitAssignsByUserIdAndStatusNotCancelled(userVO.getId(),
            LOCALE.getLanguage());
    }

    @Test
    void getUserShoppingAndCustomShoppingLists_ValidIdAndLocale_ReturnsOk() throws Exception {
        UserShoppingAndCustomShoppingListsDto response = getUserShoppingAndCustomShoppingListsDto();

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitAssignService.getUserShoppingAndCustomShoppingLists(userVO.getId(), 1L, LOCALE.getLanguage()))
            .thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{habitAssignId}/allUserAndCustomList", 1L)
            .principal(principal)
            .locale(LOCALE)
            .param("lang", LOCALE.getLanguage())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.userShoppingListItemDto[0].id")
                .value(response.getUserShoppingListItemDto().getFirst().getId()))
            .andExpect(jsonPath("$.customShoppingListItemDto[0].text")
                .value(response.getCustomShoppingListItemDto().getFirst().getText()));

        verify(habitAssignService).getUserShoppingAndCustomShoppingLists(userVO.getId(), 1L, LOCALE.getLanguage());
    }

    @Test
    void updateUserAndCustomShoppingLists_ValidRequest_ReturnsOk() throws Exception {
        UserShoppingAndCustomShoppingListsDto request = getUserShoppingAndCustomShoppingListsDto();

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);

        mockMvc.perform(put(BASE_URL + "/{habitAssignId}/allUserAndCustomList", 1L)
            .principal(principal)
            .locale(LOCALE)
            .param("lang", LOCALE.getLanguage())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(habitAssignService).fullUpdateUserAndCustomShoppingLists(
            userVO.getId(), 1L, request, LOCALE.getLanguage());
    }

    @Test
    void getListOfUserAndCustomShoppingListsInprogress_ValidLocale_ReturnsOk() throws Exception {
        List<UserShoppingAndCustomShoppingListsDto> response =
            List.of(getUserShoppingAndCustomShoppingListsDto());

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitAssignService.getListOfUserAndCustomShoppingListsWithStatusInprogress(
            userVO.getId(), LOCALE.getLanguage())).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/allUserAndCustomShoppingListsInprogress")
            .principal(principal)
            .locale(LOCALE)
            .param("lang", LOCALE.getLanguage())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].userShoppingListItemDto[0].id")
                .value(response.getFirst().getUserShoppingListItemDto().getFirst().getId()));

        verify(habitAssignService).getListOfUserAndCustomShoppingListsWithStatusInprogress(
            userVO.getId(), LOCALE.getLanguage());
    }

    @Test
    void getAllHabitAssignsByHabitIdAndAcquired_ValidHabitIdAndLocale_ReturnsOk() throws Exception {
        List<HabitAssignDto> response = List.of(getHabitAssignDto());

        when(habitAssignService.getAllHabitAssignsByHabitIdAndStatusNotCancelled(1L, LOCALE.getLanguage()))
            .thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{habitId}/all", 1L)
            .locale(LOCALE)
            .param("lang", LOCALE.getLanguage())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(response.getFirst().getId()));

        verify(habitAssignService).getAllHabitAssignsByHabitIdAndStatusNotCancelled(1L, LOCALE.getLanguage());
    }

    @Test
    void getHabitAssignByHabitId_ValidHabitIdAndLocale_ReturnsOk() throws Exception {
        HabitAssignDto response = getHabitAssignDto();

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitAssignService.findHabitAssignByUserIdAndHabitId(userVO.getId(), 1L, LOCALE.getLanguage()))
            .thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{habitId}/active", 1L)
            .principal(principal)
            .locale(LOCALE)
            .param("lang", LOCALE.getLanguage())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(response.getId()))
            .andExpect(jsonPath("$.habitStreak").value(response.getHabitStreak()));

        verify(habitAssignService).findHabitAssignByUserIdAndHabitId(userVO.getId(), 1L, LOCALE.getLanguage());
    }

    @Test
    void getUsersHabitByHabitAssignId_ValidIdAndLocale_ReturnsOk() throws Exception {
        HabitDto response = getHabitDto();

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitAssignService.findHabitByUserIdAndHabitAssignId(userVO.getId(), 1L, LOCALE.getLanguage()))
            .thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{habitAssignId}/more", 1L)
            .principal(principal)
            .locale(LOCALE)
            .param("lang", LOCALE.getLanguage())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(response.getId()))
            .andExpect(jsonPath("$.habitTranslation.name").value(response.getHabitTranslation().getName()));

        verify(habitAssignService).findHabitByUserIdAndHabitAssignId(userVO.getId(), 1L, LOCALE.getLanguage());
    }

    @Test
    void updateAssignByHabitId_ValidStatus_ReturnsOk() throws Exception {
        HabitAssignStatDto request = HabitAssignStatDto.builder()
            .status(HabitAssignStatus.ACQUIRED)
            .build();
        HabitAssignManagementDto response = getHabitAssignManagementDto();
        response.setStatus(HabitAssignStatus.ACQUIRED);

        when(habitAssignService.updateStatusByHabitAssignId(1L, request)).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/{habitAssignId}", 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(response.getId()))
            .andExpect(jsonPath("$.status").value(HabitAssignStatus.ACQUIRED.name()));

        verify(habitAssignService).updateStatusByHabitAssignId(1L, request);
    }

    @Test
    void updateAssignByHabitId_MissingStatus_ReturnsBadRequest() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{habitAssignId}", 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
            .andExpect(status().isBadRequest());

        verify(habitAssignService, never()).updateStatusByHabitAssignId(any(), any());
    }

    @Test
    void enrollHabit_ValidDateAndLocale_ReturnsOk() throws Exception {
        HabitAssignDto response = getHabitAssignDto();

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitAssignService.enrollHabit(1L, userVO.getId(), TEST_DATE, LOCALE.getLanguage()))
            .thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/{habitAssignId}/enroll/{date}", 1L, TEST_DATE)
            .principal(principal)
            .locale(LOCALE)
            .param("lang", LOCALE.getLanguage())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(response.getId()));

        verify(habitAssignService).enrollHabit(1L, userVO.getId(), TEST_DATE, LOCALE.getLanguage());
    }

    @Test
    void unenrollHabit_ValidDate_ReturnsOk() throws Exception {
        HabitAssignDto response = getHabitAssignDto();

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitAssignService.unenrollHabit(1L, userVO.getId(), TEST_DATE)).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/{habitAssignId}/unenroll/{date}", 1L, TEST_DATE)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(response.getId()))
            .andExpect(jsonPath("$.status").value(response.getStatus().name()));

        verify(habitAssignService).unenrollHabit(1L, userVO.getId(), TEST_DATE);
    }

    @Test
    void getInprogressHabitAssignOnDate_ValidDateAndLocale_ReturnsOk() throws Exception {
        List<HabitAssignDto> response = List.of(getHabitAssignDto());

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitAssignService.findInprogressHabitAssignsOnDate(userVO.getId(), TEST_DATE, LOCALE.getLanguage()))
            .thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/active/{date}", TEST_DATE)
            .principal(principal)
            .locale(LOCALE)
            .param("lang", LOCALE.getLanguage())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(response.getFirst().getId()));

        verify(habitAssignService).findInprogressHabitAssignsOnDate(userVO.getId(), TEST_DATE, LOCALE.getLanguage());
    }

    @Test
    void getHabitAssignBetweenDates_ValidRangeAndLocale_ReturnsOk() throws Exception {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);
        List<HabitsDateEnrollmentDto> response = List.of(HabitsDateEnrollmentDto.builder()
            .enrollDate(TEST_DATE)
            .habitAssigns(Collections.emptyList())
            .build());

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitAssignService.findHabitAssignsBetweenDates(userVO.getId(), from, to, LOCALE.getLanguage()))
            .thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/activity/{from}/to/{to}", from, to)
            .principal(principal)
            .locale(LOCALE)
            .param("lang", LOCALE.getLanguage())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].enrollDate").value(TEST_DATE.toString()));

        verify(habitAssignService).findHabitAssignsBetweenDates(userVO.getId(), from, to, LOCALE.getLanguage());
    }

    @Test
    void cancelHabitAssign_ValidHabitId_ReturnsOk() throws Exception {
        HabitAssignDto response = getHabitAssignDto();
        response.setStatus(HabitAssignStatus.CANCELLED);

        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);
        when(habitAssignService.cancelHabitAssign(1L, userVO.getId())).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/cancel/{habitId}", 1L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(response.getId()))
            .andExpect(jsonPath("$.status").value(HabitAssignStatus.CANCELLED.name()));

        verify(habitAssignService).cancelHabitAssign(1L, userVO.getId());
    }

    @Test
    void deleteHabitAssign_ValidId_ReturnsOk() throws Exception {
        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);

        mockMvc.perform(delete(BASE_URL + "/delete/{habitAssignId}", 1L)
                        .principal(principal))
                .andExpect(status().isOk());

        verify(habitAssignService).deleteHabitAssign(1L, userVO.getId());
    }

    @Test
    void updateShoppingListStatus_ValidRequest_ReturnsOk() throws Exception {
        UpdateUserShoppingListDto request = getUpdateUserShoppingListDto();

        mockMvc.perform(put(BASE_URL + "/saveShoppingListForHabitAssign")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        ArgumentCaptor<UpdateUserShoppingListDto> captor = ArgumentCaptor.forClass(UpdateUserShoppingListDto.class);
        verify(habitAssignService).updateUserShoppingListItem(captor.capture());
        assertEquals(request.getHabitAssignId(), captor.getValue().getHabitAssignId());
        assertEquals(request.getUserShoppingListItemId(), captor.getValue().getUserShoppingListItemId());
        assertEquals(request.getUserShoppingListAdvanceDto().getFirst().getStatus(),
            captor.getValue().getUserShoppingListAdvanceDto().getFirst().getStatus());
    }

    @Test
    void updateProgressNotificationHasDisplayed_ValidId_ReturnsOk() throws Exception {
        when(userService.findByEmail(PRINCIPAL_EMAIL)).thenReturn(userVO);

        mockMvc.perform(put(BASE_URL + "/{habitAssignId}/updateProgressNotificationHasDisplayed", 1L)
                        .principal(principal))
                .andExpect(status().isOk());

        verify(habitAssignService).updateProgressNotificationHasDisplayed(1L, userVO.getId());
    }

    private HabitAssignManagementDto getHabitAssignManagementDto() {
        return HabitAssignManagementDto.builder()
            .id(1L)
            .status(HabitAssignStatus.INPROGRESS)
            .createDateTime(ZonedDateTime.parse("2026-06-01T10:00:00Z"))
            .habitId(2L)
            .userId(userVO.getId())
            .duration(14)
            .workingDays(5)
            .habitStreak(3)
            .lastEnrollment(ZonedDateTime.parse("2026-06-10T10:00:00Z"))
            .progressNotificationHasDisplayed(false)
            .build();
    }

    private HabitAssignDto getHabitAssignDto() {
        return HabitAssignDto.builder()
            .id(1L)
            .userId(userVO.getId())
            .status(HabitAssignStatus.INPROGRESS)
            .duration(14)
            .habitStreak(3)
            .workingDays(5)
            .createDateTime(ZonedDateTime.parse("2026-06-01T10:00:00Z"))
            .lastEnrollmentDate(ZonedDateTime.parse("2026-06-10T10:00:00Z"))
            .progressNotificationHasDisplayed(false)
            .build();
    }

    private HabitDto getHabitDto() {
        return HabitDto.builder()
            .id(2L)
            .complexity(2)
            .defaultDuration(14)
            .image("habit.jpg")
            .isCustomHabit(false)
            .habitTranslation(HabitTranslationDto.builder()
                .name("Use reusable bag")
                .description("Description")
                .habitItem("Item")
                .languageCode(LOCALE.getLanguage())
                .build())
            .build();
    }
}
