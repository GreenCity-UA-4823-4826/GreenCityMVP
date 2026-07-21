package greencity.controller;

import greencity.ModelUtils;
import greencity.converters.UserArgumentResolver;
import greencity.dto.user.UserVO;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.FriendshipService;
import greencity.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FriendshipControllerTest {
    private static final String FRIENDS_LINK = "/friends";

    @InjectMocks
    private FriendshipController friendshipController;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ErrorAttributes errorAttributes;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Principal principal = () -> "test@gmail.com";

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(friendshipController)
                .setCustomArgumentResolvers(
                        new UserArgumentResolver(userService, modelMapper))
                .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
                .build();
    }

    @Test
    void getFriendsCount_validRequest_returnsOk() throws Exception {
        UserVO userVO = ModelUtils.getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(friendshipService.countOfUserFriends(any())).thenReturn(5L);

        mockMvc.perform(get(FRIENDS_LINK + "/count")
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(friendshipService, times(1)).countOfUserFriends(any());
    }
}
