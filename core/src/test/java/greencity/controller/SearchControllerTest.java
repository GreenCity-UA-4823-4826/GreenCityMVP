package greencity.controller;

import greencity.dto.PageableDto;
import greencity.dto.search.SearchNewsDto;
import greencity.dto.search.SearchResponseDto;
import greencity.dto.user.EcoNewsAuthorDto;
import greencity.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    private MockMvc mockMvc;

    @Mock
    SearchService searchService;

    @Mock
    private Validator mockValidator;

    public static final String searchLink = "/search";
    public static final String ecoNewsLink = "/econews";
    public static final String queryParamSearchEco = "?searchQuery=eco";
    public static final String queryParamSearchEcoPaginated = "?searchQuery=eco&page=0&size=5";

    private final List<SearchNewsDto> news = Stream.of(
                    new SearchNewsDto(0L,
                            "Summer is coming, get ready",
                            new EcoNewsAuthorDto(0L, "Valera Borov"),
                            ZonedDateTime.now().minusDays(9),
                            List.of("#summer", "#getting_ready")),
                    new SearchNewsDto(1L,
                            "Eco bus summer parking schedule",
                            new EcoNewsAuthorDto(1L, "Maslach"),
                            ZonedDateTime.now().minusDays(20),
                            List.of("#summer", "#bus_stop")),
                    new SearchNewsDto(2L,
                            "EV performance differences in comparing with classic approach",
                            new EcoNewsAuthorDto(2L, "Micro Barbers fellow"),
                            ZonedDateTime.now().minusDays(9),
                            List.of("#driving"))
                    ,
                    new SearchNewsDto(3L,
                            "EV performance differences in comparing with classic approach",
                            new EcoNewsAuthorDto(2L, "Micro Barbers fellow"),
                            ZonedDateTime.now().minusDays(9),
                            List.of("#driving"))
                    ,
                    new SearchNewsDto(4L,
                            "EV performance differences in comparing with classic approach",
                            new EcoNewsAuthorDto(2L, "Micro Barbers fellow"),
                            ZonedDateTime.now().minusDays(10),
                            List.of("#driving"))
                    ,
                    new SearchNewsDto(5L,
                            "EV performance differences in comparing with classic approach",
                            new EcoNewsAuthorDto(2L, "Micro Barbers fellow"),
                            ZonedDateTime.now().minusDays(11),
                            List.of("#driving"))
                    ,
                    new SearchNewsDto(6L,
                            "EV performance differences in comparing with classic approach",
                            new EcoNewsAuthorDto(2L, "Micro Barbers fellow"),
                            ZonedDateTime.now().minusDays(12),
                            List.of("#driving"))
                    ,
                    new SearchNewsDto(7L,
                            "EV performance differences in comparing with classic approach",
                            new EcoNewsAuthorDto(2L, "Micro Barbers fellow"),
                            ZonedDateTime.now().minusDays(13),
                            List.of("#driving"))
                    ,
                    new SearchNewsDto(8L,
                            "EV performance differences in comparing with classic approach",
                            new EcoNewsAuthorDto(2L, "Micro Barbers fellow"),
                            ZonedDateTime.now().minusDays(14),
                            List.of("#driving"))
                    ,
                    new SearchNewsDto(9L,
                            "EV performance differences in comparing with classic approach",
                            new EcoNewsAuthorDto(2L, "Micro Barbers fellow"),
                            ZonedDateTime.now().minusDays(15),
                            List.of("#driving"))
            )
            .collect(Collectors.toCollection(ArrayList::new));

    private final PageableDto<SearchNewsDto> pageableDto
            = new PageableDto<>(news, 5L, 0, 2);


    @BeforeEach
    void setup() {
        var controller = new SearchController(searchService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(mockValidator)
                .build();
    }

    //GET /search?searchQuery=eco → should return SearchResponseDto
    @Test
    void searchEco() throws Exception {
        when(searchService.search("eco", Locale.ENGLISH.getLanguage()))
                .thenReturn(SearchResponseDto.builder()
                        .ecoNews(news)
                        .countOfResults(10L)
                        .build());

        mockMvc.perform(get(searchLink + queryParamSearchEco)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ecoNews[*].title").isNotEmpty())
                .andExpect(jsonPath("$.ecoNews[*].author.id").isNotEmpty())
                .andExpect(jsonPath("$.ecoNews[*].author.name").isNotEmpty())
                .andExpect(jsonPath("$.ecoNews[*].creationDate").isNotEmpty())
                .andExpect(jsonPath("$.ecoNews[*].tags").isNotEmpty());
    }

    //GET /search/econews?searchQuery=eco&page=0&size=5 → should return paginated PageableDto<SearchNewsDto>
    @Test
    void searchEcoPaginated() throws Exception {
        when(searchService.searchAllNews(any(Pageable.class),
                eq("eco"),
                eq(Locale.ENGLISH.getLanguage()))
        ).thenReturn(pageableDto);

        mockMvc.perform(get(searchLink + ecoNewsLink + queryParamSearchEcoPaginated)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                //.andExpect(jsonPath())
        ;
    }

    //Invalid/missing searchQuery → should return 400 BAD REQUEST (validation test, optional)

    //Locale resolution – verify it is passed to the service

    //Verify interactions with SearchService.search(...) and searchAllNews(...)
}
