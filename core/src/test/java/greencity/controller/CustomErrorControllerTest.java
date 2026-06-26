package greencity.controller;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomErrorControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private CustomErrorController customErrorController;

    private static final String ERROR_PATH = "/error";

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(customErrorController)
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .build();
    }

    @Test
    void errorWithNoErrorContext_returns200() throws Exception {
        mockMvc.perform(get(ERROR_PATH))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.error").value("OK"));
    }

    @Test
    void errorWithStatusCode404_returns404() throws Exception {
        mockMvc.perform(get(ERROR_PATH)
            .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void errorWithStatusCode500_returns500() throws Exception {
        mockMvc.perform(get(ERROR_PATH)
            .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void errorWithNonNumericStatusCode_returns500() throws Exception {
        mockMvc.perform(get(ERROR_PATH)
            .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, "not-a-number"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void errorWithUnknownStatusCode_returns500() throws Exception {
        mockMvc.perform(get(ERROR_PATH)
            .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 999))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }
}