package greencity.controller;

import greencity.ModelUtils;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.service.HabitFactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.http.MediaType;

import org.springframework.validation.Validator;

import java.util.Locale;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;


@ExtendWith(MockitoExtension.class)
class HabitFactControllerTest {
    private static final String habitFactLink = "/facts";

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

        when(habitFactService.getRandomHabitFactByHabitIdAndLanguage(habitId, locale.getLanguage()))
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
                .getRandomHabitFactByHabitIdAndLanguage(habitId, locale.getLanguage());
    }

    @Test
    void delete_ValidHabitFactId_ReturnsOk() throws Exception {
        Long id = 2L;

        mockMvc.perform(delete(habitFactLink + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(habitFactService, times(1)).delete(id);
        verifyNoMoreInteractions(habitFactService);
    }

    @Test
    void delete_InvalidHabitFactId_ReturnsBadRequest() throws Exception {
        String invalidId = "abc";

        mockMvc.perform(delete(habitFactLink + "/{id}", invalidId))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(habitFactService);
    }
}

