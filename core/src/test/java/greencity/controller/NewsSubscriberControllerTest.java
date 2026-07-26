package greencity.controller;

import greencity.service.NewsSubscriberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NewsSubscriberControllerTest {
    private static final String URL = "/news-subscribers";

    private MockMvc mockMvc;

    @Mock
    private NewsSubscriberService newsSubscriberService;

    @InjectMocks
    private NewsSubscriberController newsSubscriberController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(newsSubscriberController).build();
    }

    @Test
    void subscribeAcceptsValidEmail() throws Exception {
        mockMvc.perform(post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"user@example.com\"}"))
            .andExpect(status().isOk());

        verify(newsSubscriberService).subscribe("user@example.com");
    }

    @Test
    void subscribeTrimsEmailBeforeValidation() throws Exception {
        mockMvc.perform(post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"  user@example.com  \"}"))
            .andExpect(status().isOk());

        verify(newsSubscriberService).subscribe("user@example.com");
    }

    @Test
    void subscribeRejectsEmptyEmail() throws Exception {
        assertInvalidEmail("");
    }

    @Test
    void subscribeRejectsMalformedEmail() throws Exception {
        assertInvalidEmail("invalid-email");
    }

    @Test
    void subscribeRejectsEmailWithoutTopLevelDomain() throws Exception {
        assertInvalidEmail("user@example");
    }

    @Test
    void subscribeRejectsEmailWithInternalSpace() throws Exception {
        assertInvalidEmail("user @example.com");
    }

    @Test
    void unsubscribeReturnsNoContent() throws Exception {
        mockMvc.perform(delete(URL + "/token"))
            .andExpect(status().isNoContent());

        verify(newsSubscriberService).unsubscribe("token");
    }

    @Test
    void unsubscribeByLinkReturnsConfirmationPage() throws Exception {
        mockMvc.perform(get(URL + "/unsubscribe")
            .param("token", "token"))
            .andExpect(status().isOk());

        verify(newsSubscriberService).unsubscribe("token");
    }

    private void assertInvalidEmail(String email) throws Exception {
        mockMvc.perform(post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\"}"))
            .andExpect(status().isBadRequest());

        verify(newsSubscriberService, never()).subscribe(org.mockito.ArgumentMatchers.anyString());
    }
}
