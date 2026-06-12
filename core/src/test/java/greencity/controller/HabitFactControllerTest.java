package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.dto.PageableDto;
import greencity.dto.habitfact.HabitFactDtoResponse;
import greencity.dto.habitfact.HabitFactPostDto;
import greencity.dto.habitfact.HabitFactUpdateDto;
import greencity.dto.habitfact.HabitFactVO;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.service.HabitFactService;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HabitFactControllerTest {

    private static final String habitFactLink = "/facts";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private HabitFactService habitFactService;

    @Mock
    private Validator mockValidator;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private HabitFactController habitFactController;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(habitFactController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setValidator(mockValidator)
            .build();
    }

    @Test
    void getHabitFactOfTheDay_ValidLanguageId_ReturnsOk() throws Exception {
        Long languageId = 2L;
        LanguageTranslationDTO responseDto = ModelUtils.getLanguageTranslationDTO();

        when(habitFactService.getHabitFactOfTheDay(languageId)).thenReturn(responseDto);

        mockMvc.perform(get(habitFactLink + "/dayFact/{languageId}", languageId)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").value(responseDto.getContent()))
            .andExpect(jsonPath("$.language.code").value(responseDto.getLanguage().getCode()));

        verify(habitFactService, times(1)).getHabitFactOfTheDay(languageId);
    }

    @Test
    void getRandomFactByHabitId_ValidHabitIdAndLocale_ReturnsOk() throws Exception {
        Long habitId = 2L;
        Locale locale = Locale.ENGLISH;
        LanguageTranslationDTO responseDto = ModelUtils.getLanguageTranslationDTO();

        String languageCode = locale.getLanguage();

        when(habitFactService.getRandomHabitFactByHabitIdAndLanguage(habitId, languageCode))
            .thenReturn(responseDto);

        mockMvc.perform(get(habitFactLink + "/random/{habitId}", habitId)
            .locale(locale)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").value(responseDto.getContent()))
            .andExpect(jsonPath("$.language.id").value(responseDto.getLanguage().getId()))
            .andExpect(jsonPath("$.language.code").value(responseDto.getLanguage().getCode()));

        verify(habitFactService, times(1))
            .getRandomHabitFactByHabitIdAndLanguage(habitId, languageCode);
    }

    @Test
    void getAll_ValidPageableAndLocale_ReturnsOk() throws Exception {
        Locale locale = Locale.ENGLISH;
        String languageCode = locale.getLanguage();
        LanguageTranslationDTO responseDto = ModelUtils.getLanguageTranslationDTO();
        PageableDto<LanguageTranslationDTO> pageableDto =
            new PageableDto<>(List.of(responseDto), 1L, 0, 1);
        when(habitFactService.getAllHabitFacts(any(Pageable.class), eq(languageCode)))
            .thenReturn(pageableDto);
        mockMvc.perform(get(habitFactLink)
            .param("page", "0")
            .param("size", "10")
            .locale(locale)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalElements").value(pageableDto.getTotalElements()))
            .andExpect(jsonPath("$.currentPage").value(pageableDto.getCurrentPage()))
            .andExpect(jsonPath("$.totalPages").value(pageableDto.getTotalPages()))
            .andExpect(jsonPath("$.page[0].content").value(responseDto.getContent()))
            .andExpect(jsonPath("$.page[0].language.code").value(responseDto.getLanguage().getCode()));

        verify(habitFactService, times(1))
            .getAllHabitFacts(any(Pageable.class), eq(languageCode));
    }

    @Test
    void save_ValidHabitFactPostDto_ReturnsCreated() throws Exception {
        HabitFactPostDto requestDto = ModelUtils.getHabitFactPostDto();
        HabitFactVO habitFactVO = ModelUtils.getHabitFactVO();
        HabitFactDtoResponse responseDto = HabitFactDtoResponse.builder()
            .id(1L)
            .build();
        when(habitFactService.save(any(HabitFactPostDto.class))).thenReturn(habitFactVO);
        when(modelMapper.map(habitFactVO, HabitFactDtoResponse.class)).thenReturn(responseDto);

        mockMvc.perform(post(habitFactLink)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(responseDto.getId()));

        verify(habitFactService, times(1)).save(any(HabitFactPostDto.class));
        verify(modelMapper, times(1)).map(habitFactVO, HabitFactDtoResponse.class);
    }

    @Test
    void update_ValidHabitFactUpdateDto_ReturnsOk() throws Exception {
        Long id = 1L;
        HabitFactUpdateDto requestDto = ModelUtils.getHabitFactUpdateDto();
        HabitFactVO habitFactVO = ModelUtils.getHabitFactVO();
        HabitFactPostDto responseDto = ModelUtils.getHabitFactPostDto();

        when(habitFactService.update(any(HabitFactUpdateDto.class), eq(id)))
            .thenReturn(habitFactVO);
        when(modelMapper.map(habitFactVO, HabitFactPostDto.class))
            .thenReturn(responseDto);

        mockMvc.perform(put(habitFactLink + "/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.habit.id").value(responseDto.getHabit().getId()))
            .andExpect(
                jsonPath("$.translations[0].content").value(responseDto.getTranslations().getFirst().getContent()));
        verify(habitFactService, times(1))
            .update(any(HabitFactUpdateDto.class), eq(id));
        verify(modelMapper, times(1))
            .map(habitFactVO, HabitFactPostDto.class);
    }

    @Test
    void delete_ValidHabitFactId_ReturnsOk() throws Exception {
        Long id = 2L;

        mockMvc.perform(delete(habitFactLink + "/{id}", id))
            .andExpect(status().isOk())
            .andExpect(content().string(""));

        verify(habitFactService, times(1)).delete(id);
    }

    @Test
    void delete_InvalidHabitFactId_ReturnsBadRequest() throws Exception {
        String invalidId = "abc";

        mockMvc.perform(delete(habitFactLink + "/{id}", invalidId))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(habitFactService);
    }
}