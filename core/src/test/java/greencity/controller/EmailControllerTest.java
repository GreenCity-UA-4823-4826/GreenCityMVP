package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.email.ChangePlaceStatusDto;
import java.security.Principal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static greencity.ModelUtils.getPrincipal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailControllerTest {
    private static final String EMAIL_LINK = "/email";

    private MockMvc mockMvc;

    @InjectMocks
    private EmailController emailController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Principal principal = getPrincipal();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(emailController).build();
    }

    @Test
    void changePlaceStatusTest() throws Exception {
        ChangePlaceStatusDto dto = new ChangePlaceStatusDto();
        dto.setAuthorEmail("Admin1@gmail.com");
        dto.setAuthorFirstName("Admin");
        dto.setPlaceName("hoho");
        dto.setPlaceStatus("string");

        mockMvc.perform(post(EMAIL_LINK + "/changePlaceStatus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .principal(principal))
            .andExpect(status().isOk());
    }
}
