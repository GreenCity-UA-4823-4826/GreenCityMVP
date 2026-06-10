package greencity.mapping;

import greencity.dto.search.SearchNewsDto;
import greencity.entity.EcoNews;
import greencity.entity.User;
import greencity.entity.Tag;
import greencity.entity.localization.TagTranslation;
import greencity.entity.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SearchNewsDtoMapperTest {
    private final SearchNewsDtoMapper mapper = new SearchNewsDtoMapper();

    @BeforeEach
    void setUp() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void convertWithEnglishLocaleTest() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        Language enLang = Language.builder().id(1L).code("en").build();
        Language uaLang = Language.builder().id(2L).code("ua").build();

        TagTranslation enTag = TagTranslation.builder().name("News").language(enLang).build();
        TagTranslation uaTag = TagTranslation.builder().name("Новини").language(uaLang).build();

        Tag tag = Tag.builder()
            .id(1L)
            .tagTranslations(Arrays.asList(enTag, uaTag))
            .build();

        User author = User.builder().id(1L).name("Test Author").build();
        ZonedDateTime now = ZonedDateTime.now();

        EcoNews ecoNews = EcoNews.builder()
            .id(1L)
            .title("Test Title")
            .author(author)
            .creationDate(now)
            .tags(Collections.singletonList(tag))
            .build();

        SearchNewsDto result = mapper.convert(ecoNews);

        assertEquals(1L, result.getId());
        assertEquals("Test Title", result.getTitle());
        assertEquals(1L, result.getAuthor().getId());
        assertEquals("Test Author", result.getAuthor().getName());
        assertEquals(now, result.getCreationDate());
        assertEquals(Collections.singletonList("News"), result.getTags());
    }

    @Test
    void convertWithUkrainianLocaleTest() {
        LocaleContextHolder.setLocale(Locale.of("ua"));

        Language enLang = Language.builder().id(1L).code("en").build();
        Language uaLang = Language.builder().id(2L).code("ua").build();

        TagTranslation enTag = TagTranslation.builder().name("News").language(enLang).build();
        TagTranslation uaTag = TagTranslation.builder().name("Новини").language(uaLang).build();

        Tag tag = Tag.builder()
            .id(1L)
            .tagTranslations(Arrays.asList(enTag, uaTag))
            .build();

        User author = User.builder().id(1L).name("Author").build();

        EcoNews ecoNews = EcoNews.builder()
            .id(1L)
            .title("Title")
            .author(author)
            .creationDate(ZonedDateTime.now())
            .tags(Collections.singletonList(tag))
            .build();

        SearchNewsDto result = mapper.convert(ecoNews);

        assertEquals(Collections.singletonList("Новини"), result.getTags());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSourceTest() {
        assertThrows(NullPointerException.class, () -> mapper.convert((EcoNews) null));
    }
}
