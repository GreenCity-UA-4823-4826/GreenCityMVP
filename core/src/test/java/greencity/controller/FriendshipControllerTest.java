package greencity.controller;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import greencity.ModelUtils;
import greencity.converters.UserArgumentResolver;
import greencity.dto.user.UserFriendDto;
import greencity.dto.user.UserVO;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.FriendshipService;
import greencity.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.data.domain.PageRequest;
import org.mockito.Mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.security.Principal;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        objectMapper.registerModule(new JavaTimeModule());
        MappingJackson2HttpMessageConverter converter =
            new MappingJackson2HttpMessageConverter(objectMapper);

        this.mockMvc = MockMvcBuilders
            .standaloneSetup(friendshipController)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .setMessageConverters(converter)
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

    @Test
    void searchFriends_validRequest_returnsOk() throws Exception {
        UserVO userVO = ModelUtils.getUserVO();
        UserFriendDto dto = UserFriendDto.builder()
                .id(2L)
                .name("Kristin Watson")
                .city("Lviv")
                .rating(658.0)
                .mutualFriendsCount(8L)
                .build();

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(friendshipService.searchFriends(any(), anyString(), any()))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1));

        mockMvc.perform(get(FRIENDS_LINK + "/search")
                        .param("query", "Kristin")
                        .param("page", "0")
                        .param("size", "10")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(friendshipService, times(1)).searchFriends(any(), anyString(), any());
    }

    @Test
    void addFriend_validRequest_returnsCreated() throws Exception {
        UserVO userVO = ModelUtils.getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        doNothing().when(friendshipService).addFriend(any(), anyLong());

        mockMvc.perform(post(FRIENDS_LINK + "/2")
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        verify(friendshipService, times(1)).addFriend(any(), eq(2L));
    }

    @Test
    void cancelFriendRequest_validRequest_returnsOk() throws Exception {
        UserVO userVO = ModelUtils.getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        doNothing().when(friendshipService).cancelFriendRequest(any(), anyLong());

        mockMvc.perform(delete(FRIENDS_LINK + "/2")
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(friendshipService, times(1)).cancelFriendRequest(any(), eq(2L));
    }
}
