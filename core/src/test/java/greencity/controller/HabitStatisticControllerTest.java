package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import greencity.converters.UserArgumentResolver;
import greencity.dto.habitstatistic.AddHabitStatisticDto;
import greencity.dto.habitstatistic.GetHabitStatisticDto;
import greencity.dto.habitstatistic.HabitItemsAmountStatisticDto;
import greencity.dto.habitstatistic.HabitStatisticDto;
import greencity.dto.habitstatistic.UpdateHabitStatisticDto;
import greencity.dto.user.UserVO;
import greencity.enums.HabitRate;
import greencity.service.HabitStatisticService;
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
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.ZonedDateTime;
import java.util.Collections;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HabitStatisticControllerTest {

    private static final String BASE_URL = "/habit/statistic";

    private MockMvc mockMvc;

    @InjectMocks
    private HabitStatisticController habitStatisticController;

    @Mock
    private HabitStatisticService habitStatisticService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final Principal principal = getPrincipal();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(habitStatisticController)
            .setCustomArgumentResolvers(new UserArgumentResolver(userService, modelMapper))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
    }

    @Test
    void findAllByHabitId_ValidHabitId_ReturnsGetHabitStatisticDtoWithStatusOk() throws Exception {
        GetHabitStatisticDto responseDto = GetHabitStatisticDto.builder()
            .amountOfUsersAcquired(3L)
            .habitStatisticDtoList(Collections.emptyList())
            .build();

        when(habitStatisticService.findAllStatsByHabitId(1L)).thenReturn(responseDto);

        mockMvc.perform(get(BASE_URL + "/{habitId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.amountOfUsersAcquired").value(3))
            .andExpect(jsonPath("$.habitStatisticDtoList").isArray());

        verify(habitStatisticService).findAllStatsByHabitId(1L);
    }

    @Test
    void findAllStatsByHabitAssignId_ValidHabitAssignId_ReturnsListWithStatusOk() throws Exception {
        HabitStatisticDto dto = HabitStatisticDto.builder()
            .id(1L)
            .habitRate(HabitRate.GOOD)
            .createDate(ZonedDateTime.now())
            .amountOfItems(5)
            .habitAssignId(2L)
            .build();

        when(habitStatisticService.findAllStatsByHabitAssignId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get(BASE_URL + "/assign/{habitAssignId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].amountOfItems").value(5))
            .andExpect(jsonPath("$[0].habitRate").value(HabitRate.GOOD.name()));

        verify(habitStatisticService).findAllStatsByHabitAssignId(1L);
    }

    @Test
    void findAllStatsByHabitAssignId_NoStatistics_ReturnsEmptyListWithStatusOk() throws Exception {
        when(habitStatisticService.findAllStatsByHabitAssignId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_URL + "/assign/{habitAssignId}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        verify(habitStatisticService).findAllStatsByHabitAssignId(1L);
    }

    @Test
    void saveHabitStatistic_ValidRequest_ReturnsHabitStatisticDtoWithStatusCreated() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        AddHabitStatisticDto requestDto = AddHabitStatisticDto.builder()
            .amountOfItems(5)
            .habitRate(HabitRate.GOOD)
            .createDate(ZonedDateTime.now())
            .build();

        HabitStatisticDto responseDto = HabitStatisticDto.builder()
            .id(1L)
            .amountOfItems(5)
            .habitRate(HabitRate.GOOD)
            .createDate(ZonedDateTime.now())
            .habitAssignId(1L)
            .build();

        when(habitStatisticService.saveByHabitIdAndUserId(eq(1L), eq(userVO.getId()),
            any(AddHabitStatisticDto.class))).thenReturn(responseDto);

        mockMvc.perform(post(BASE_URL + "/{habitId}", 1L)
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.amountOfItems").value(5));

        verify(userService).findByEmail("test@gmail.com");
        verify(habitStatisticService).saveByHabitIdAndUserId(eq(1L), eq(userVO.getId()),
            any(AddHabitStatisticDto.class));
    }

    @Test
    void saveHabitStatistic_InvalidBody_ReturnsStatusBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL + "/{habitId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void saveHabitStatistic_AmountOfItemsExceedsMax_ReturnsStatusBadRequest() throws Exception {
        String json = "{\"amountOfItems\":17,\"habitRate\":\"GOOD\",\"createDate\":\"2024-01-01T10:00:00+00:00\"}";

        mockMvc.perform(post(BASE_URL + "/{habitId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatistic_ValidRequest_ReturnsUpdateHabitStatisticDtoWithStatusOk() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        UpdateHabitStatisticDto requestDto = UpdateHabitStatisticDto.builder()
            .amountOfItems(3)
            .habitRate(HabitRate.NORMAL)
            .build();

        when(habitStatisticService.update(eq(1L), eq(userVO.getId()),
            any(UpdateHabitStatisticDto.class))).thenReturn(requestDto);

        mockMvc.perform(put(BASE_URL + "/{id}", 1L)
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.amountOfItems").value(3))
            .andExpect(jsonPath("$.habitRate").value(HabitRate.NORMAL.name()));

        verify(userService).findByEmail("test@gmail.com");
        verify(habitStatisticService).update(eq(1L), eq(userVO.getId()), any(UpdateHabitStatisticDto.class));
    }

    @Test
    void updateStatistic_InvalidBody_ReturnsStatusBadRequest() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatistic_AmountOfItemsBelowMin_ReturnsStatusBadRequest() throws Exception {
        String json = "{\"amountOfItems\":-1,\"habitRate\":\"GOOD\"}";

        mockMvc.perform(put(BASE_URL + "/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getTodayStatisticsForAllHabitItems_ValidLocale_ReturnsListWithStatusOk() throws Exception {
        HabitItemsAmountStatisticDto dto = HabitItemsAmountStatisticDto.builder()
            .habitItem("cup")
            .notTakenItems(4L)
            .build();

        when(habitStatisticService.getTodayStatisticsForAllHabitItems("en")).thenReturn(List.of(dto));

        mockMvc.perform(get(BASE_URL + "/todayStatisticsForAllHabitItems")
                .header("Accept-Language", "en"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].habitItem").value("cup"))
            .andExpect(jsonPath("$[0].notTakenItems").value(4));

        verify(habitStatisticService).getTodayStatisticsForAllHabitItems("en");
    }

    @Test
    void getTodayStatisticsForAllHabitItems_EmptyResult_ReturnsEmptyListWithStatusOk() throws Exception {
        when(habitStatisticService.getTodayStatisticsForAllHabitItems("en"))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_URL + "/todayStatisticsForAllHabitItems")
                .header("Accept-Language", "en"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        verify(habitStatisticService).getTodayStatisticsForAllHabitItems("en");
    }

    @Test
    void findAmountOfAcquiredHabits_ValidUserId_ReturnsCountWithStatusOk() throws Exception {
        when(habitStatisticService.getAmountOfAcquiredHabitsByUserId(1L)).thenReturn(5L);

        mockMvc.perform(get(BASE_URL + "/acquired/count")
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(5));

        verify(habitStatisticService).getAmountOfAcquiredHabitsByUserId(1L);
    }

    @Test
    void findAmountOfAcquiredHabits_NoAcquiredHabits_ReturnsZeroWithStatusOk() throws Exception {
        when(habitStatisticService.getAmountOfAcquiredHabitsByUserId(1L)).thenReturn(0L);

        mockMvc.perform(get(BASE_URL + "/acquired/count")
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(0));

        verify(habitStatisticService).getAmountOfAcquiredHabitsByUserId(1L);
    }

    @Test
    void findAmountOfHabitsInProgress_ValidUserId_ReturnsCountWithStatusOk() throws Exception {
        when(habitStatisticService.getAmountOfHabitsInProgressByUserId(1L)).thenReturn(3L);

        mockMvc.perform(get(BASE_URL + "/in-progress/count")
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(3));

        verify(habitStatisticService).getAmountOfHabitsInProgressByUserId(1L);
    }

    @Test
    void findAmountOfHabitsInProgress_NoHabitsInProgress_ReturnsZeroWithStatusOk() throws Exception {
        when(habitStatisticService.getAmountOfHabitsInProgressByUserId(1L)).thenReturn(0L);

        mockMvc.perform(get(BASE_URL + "/in-progress/count")
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(0));

        verify(habitStatisticService).getAmountOfHabitsInProgressByUserId(1L);
    }
}
