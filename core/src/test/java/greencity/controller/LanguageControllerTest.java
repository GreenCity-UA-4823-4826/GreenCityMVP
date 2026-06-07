package greencity.controller;

import greencity.service.LanguageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LanguageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LanguageService languageService;

    @InjectMocks
    private LanguageController languageController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(languageController).build();
    }

    @Test
    void findAllLanguageCodesTest() throws Exception {
        // Given
        List<String> mockLanguages = List.of("en", "ua", "fr");
        when(languageService.findAllLanguageCodes()).thenReturn(mockLanguages);

        // When & Then
        mockMvc.perform(get("/language")
                        .accept(MediaType.APPLICATION_JSON)) // Явно просимо JSON
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))) // Надійна перевірка розміру масиву
                .andExpect(jsonPath("$[0]").value("en"))
                .andExpect(jsonPath("$[1]").value("ua"))
                .andExpect(jsonPath("$[2]").value("fr"));

        // Verify
        verify(languageService).findAllLanguageCodes();
    }
}