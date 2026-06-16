package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.PageableDto;
import greencity.dto.search.SearchNewsDto;
import greencity.dto.search.SearchResponseDto;
import greencity.dto.user.EcoNewsAuthorDto;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {
    private final ErrorAttributes errorAttributes = new DefaultErrorAttributes();
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private MockMvc mockMvc;

    @Mock
    SearchService searchService;

    @Mock
    private Validator mockValidator;

    @InjectMocks
    private SearchController searchController;

    private static final String searchQueryEco = "eco";
    private static final String localeEN = Locale.ENGLISH.getLanguage();
    private static final String searchLink = "/search";
    private static final String ecoNewsLink = "/econews";
    private static final String queryParamSearchEco = "?searchQuery=eco";
    private static final String queryParamSearchEmpty = "?searchQuery=\"\"";
    private static final String queryParamSearchEcoPaginated = "?searchQuery=eco&page=0&size=5";
    private static final String queryParamSearchEcoEmptyPaginated = "?searchQuery=\"\"&page=0&size=5";
    private static final String blankString = "\"\"";

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
                            List.of("#driving")),
                    new SearchNewsDto(3L,
                            "EV performance differences in comparing with classic approach",
                            new EcoNewsAuthorDto(2L, "Micro Barbers fellow"),
                            ZonedDateTime.now().minusDays(9),
                            List.of("#driving")),
                    new SearchNewsDto(4L,
                            "EV performance differences in comparing with classic approach",
                            new EcoNewsAuthorDto(2L, "Micro Barbers fellow"),
                            ZonedDateTime.now().minusDays(10),
                            List.of("#driving")),
                    new SearchNewsDto(5L,
                            "EV performance differences in comparing with classic approach",
                            new EcoNewsAuthorDto(2L, "Micro Barbers fellow"),
                            ZonedDateTime.now().minusDays(11),
                            List.of("#driving")),
                    new SearchNewsDto(6L,
                            "EV performance differences in comparing with classic approach",
                            new EcoNewsAuthorDto(2L, "Micro Barbers fellow"),
                            ZonedDateTime.now().minusDays(12),
                            List.of("#driving")),
                    new SearchNewsDto(7L,
                            "EV performance differences in comparing with classic approach",
                            new EcoNewsAuthorDto(2L, "Micro Barbers fellow"),
                            ZonedDateTime.now().minusDays(13),
                            List.of("#driving")),
                    new SearchNewsDto(8L,
                            "EV performance differences in comparing with classic approach",
                            new EcoNewsAuthorDto(2L, "Micro Barbers fellow"),
                            ZonedDateTime.now().minusDays(14),
                            List.of("#driving")),
                    new SearchNewsDto(9L,
                            "EV performance differences in comparing with classic approach",
                            new EcoNewsAuthorDto(2L, "Micro Barbers fellow"),
                            ZonedDateTime.now().minusDays(15),
                            List.of("#driving")))
            .collect(Collectors.toCollection(ArrayList::new));

    private final PageableDto<SearchNewsDto> pageableDto
            = new PageableDto<>(news, 10L, 0, 2);

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(searchController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(mockValidator)
                .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
                .build();
    }

    // GET /search?searchQuery=eco → should return SearchResponseDto
    // Verify interactions with SearchService.search(...)
    // Locale resolution – verify it is passed to the service
    @Test
    void searchEco() throws Exception {
        SearchResponseDto dto = SearchResponseDto.builder()
                .ecoNews(news)
                .countOfResults(10L)
                .build();

        when(searchService.search(searchQueryEco, localeEN))
                .thenReturn(dto);

        mockMvc.perform(get(searchLink + queryParamSearchEco)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)))
                .andExpect(jsonPath("$.ecoNews[*].title").isNotEmpty())
                .andExpect(jsonPath("$.ecoNews[*].author.id").isNotEmpty())
                .andExpect(jsonPath("$.ecoNews[*].author.name").isNotEmpty())
                .andExpect(jsonPath("$.ecoNews[*].creationDate").isNotEmpty())
                .andExpect(jsonPath("$.ecoNews[*].tags").isNotEmpty());

        verify(searchService, times(1)).search(searchQueryEco, localeEN);
    }

    // GET /search/econews?searchQuery=eco&page=0&size=5 → should return paginated PageableDto<SearchNewsDto>
    // Verify interactions with searchAllNews(...)
    // Locale resolution – verify it is passed to the service
    @Test
    void searchEcoPaginated() throws Exception {
        when(searchService.searchAllNews(any(Pageable.class), eq(searchQueryEco), eq(Locale.ENGLISH.getLanguage()))
        ).thenReturn(pageableDto);

        mockMvc.perform(get(searchLink + ecoNewsLink + queryParamSearchEcoPaginated).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(pageableDto)))
                        .andExpect(jsonPath("$.page[*].id").isNotEmpty())
                        .andExpect(jsonPath("$.page[*].title").isNotEmpty())
                        .andExpect(jsonPath("$.page[*].author").isNotEmpty())
                        .andExpect(jsonPath("$.page[*].author.id").isNotEmpty())
                        .andExpect(jsonPath("$.page[*].author.name").isNotEmpty())
                        .andExpect(jsonPath("$.page[*].creationDate").isNotEmpty())
                        .andExpect(jsonPath("$.page[*].tags").isNotEmpty())
                        .andExpect(jsonPath("$.page[*]").isNotEmpty());
        verify(searchService, times(1))
                .searchAllNews(any(Pageable.class), eq(searchQueryEco), eq(localeEN));

    }

    // Invalid/missing searchQuery → should return 400 BAD REQUEST (validation test,optional)
    @Test
    void searchNegativeBadRequest() throws Exception {
        Mockito.when(searchService.search(blankString, localeEN))
                .thenThrow(greencity.exception.exceptions.BadRequestException.class);

        Mockito.when(searchService.searchAllNews(any(Pageable.class),
                        eq(blankString),
                        eq(Locale.ENGLISH.getLanguage())))
                .thenThrow(greencity.exception.exceptions.BadRequestException.class);

        mockMvc.perform(get(searchLink + queryParamSearchEmpty))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get(searchLink + ecoNewsLink + queryParamSearchEcoEmptyPaginated))
                .andExpect(status().isBadRequest());
    }
}
