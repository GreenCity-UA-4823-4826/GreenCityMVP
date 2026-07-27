package greencity.mapping;

import greencity.dto.econews.EcoNewsDto;
import greencity.entity.EcoNews;
import greencity.entity.EcoNewsComment;
import greencity.entity.User;
import greencity.entity.Tag;
import greencity.entity.localization.TagTranslation;
import greencity.entity.Language;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EcoNewsDtoMapperTest {
    private final EcoNewsDtoMapper mapper = new EcoNewsDtoMapper();

    @Test
    void convert() {
        Language en = Language.builder().id(1L).code("en").build();
        Language ua = Language.builder().id(2L).code("ua").build();
        TagTranslation enT = TagTranslation.builder().name("News").language(en).build();
        TagTranslation uaT = TagTranslation.builder().name("Новини").language(ua).build();
        Tag tag = Tag.builder().id(1L).tagTranslations(Arrays.asList(enT, uaT)).build();
        User author = User.builder().id(1L).name("Author").build();
        EcoNewsComment c1 = EcoNewsComment.builder().id(1L).text("a").deleted(false).build();
        EcoNewsComment c2 = EcoNewsComment.builder().id(2L).text("b").deleted(true).build();
        ZonedDateTime now = ZonedDateTime.now();

        EcoNews news = EcoNews.builder()
            .id(1L).title("Title").text("Content").shortInfo("Info")
            .creationDate(now).imagePath("img.jpg").author(author)
            .tags(Collections.singletonList(tag))
            .usersLikedNews(new HashSet<>(Collections.singletonList(author)))
            .usersDislikedNews(Collections.emptySet())
            .ecoNewsComments(Arrays.asList(c1, c2))
            .build();

        EcoNewsDto result = mapper.convert(news);
        assertEquals(1L, result.getId());
        assertEquals("Title", result.getTitle());
        assertEquals("Content", result.getContent());
        assertEquals("Info", result.getShortInfo());
        assertEquals(now, result.getCreationDate());
        assertEquals("img.jpg", result.getImagePath());
        assertEquals(1L, result.getAuthor().getId());
        assertEquals(Collections.singletonList("News"), result.getTags());
        assertEquals(Collections.singletonList("Новини"), result.getTagsUa());
        assertEquals(1, result.getLikes());
        assertEquals(0, result.getDislikes());
        assertEquals(1, result.getCountComments());
    }

    @Test
    void convertWithEmptyCollections() {
        User author = User.builder().id(1L).name("A").build();
        EcoNews news = EcoNews.builder()
            .id(1L).title("T").text("C").shortInfo("I")
            .creationDate(ZonedDateTime.now()).author(author)
            .tags(Collections.emptyList())
            .usersLikedNews(Collections.emptySet())
            .usersDislikedNews(Collections.emptySet())
            .ecoNewsComments(Collections.emptyList())
            .build();
        EcoNewsDto result = mapper.convert(news);
        assertEquals(0, result.getCountComments());
        assertEquals(0, result.getLikes());
        assertEquals(0, result.getDislikes());
        assertEquals(Collections.emptyList(), result.getTags());
        assertEquals(Collections.emptyList(), result.getTagsUa());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSource() {
        assertThrows(NullPointerException.class, () -> mapper.convert((EcoNews) null));
    }
}
