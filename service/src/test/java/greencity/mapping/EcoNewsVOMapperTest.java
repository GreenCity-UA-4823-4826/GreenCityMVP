package greencity.mapping;

import greencity.dto.econews.EcoNewsVO;
import greencity.entity.EcoNews;
import greencity.entity.EcoNewsComment;
import greencity.entity.User;
import greencity.entity.Tag;
import greencity.entity.localization.TagTranslation;
import greencity.entity.Language;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EcoNewsVOMapperTest {
    private final EcoNewsVOMapper mapper = new EcoNewsVOMapper();

    @Test
    void convert() {
        Language en = Language.builder().id(1L).code("en").build();
        Language ua = Language.builder().id(2L).code("ua").build();
        TagTranslation enT = TagTranslation.builder().id(1L).name("News").language(en).build();
        TagTranslation uaT = TagTranslation.builder().id(2L).name("Новини").language(ua).build();
        Tag tag = Tag.builder().id(1L).tagTranslations(Arrays.asList(enT, uaT)).build();
        User author = User.builder().id(1L).name("Author")
            .userStatus(UserStatus.ACTIVATED).role(Role.ROLE_USER).build();
        User liked = User.builder().id(2L).build();
        ZonedDateTime now = ZonedDateTime.now();
        EcoNewsComment comment = EcoNewsComment.builder()
            .id(1L).text("comment").createdDate(LocalDateTime.now())
            .modifiedDate(LocalDateTime.now()).deleted(false).currentUserLiked(false)
            .user(author).ecoNews(EcoNews.builder().id(1L).build())
            .build();
        // Set ecoNews on comment builder doesn't work via builder because EcoNewsComment uses @SuperBuilder
        EcoNews news = EcoNews.builder()
            .id(1L).title("Title").text("Content").source("Src").imagePath("img.jpg")
            .creationDate(now).author(author).tags(Collections.singletonList(tag))
            .usersLikedNews(new HashSet<>(Collections.singletonList(liked)))
            .usersDislikedNews(Collections.emptySet())
            .ecoNewsComments(Collections.singletonList(comment))
            .build();

        EcoNewsVO result = mapper.convert(news);
        assertEquals(1L, result.getId());
        assertEquals("Title", result.getTitle());
        assertEquals("Content", result.getText());
        assertEquals("Src", result.getSource());
        assertEquals("img.jpg", result.getImagePath());
        assertEquals(now, result.getCreationDate());
        assertEquals(1L, result.getAuthor().getId());
        assertEquals("Author", result.getAuthor().getName());
        assertEquals(UserStatus.ACTIVATED, result.getAuthor().getUserStatus());
        assertEquals(Role.ROLE_USER, result.getAuthor().getRole());
        assertEquals(1, result.getTags().size());
        assertEquals(1, result.getUsersLikedNews().size());
        assertTrue(result.getUsersLikedNews().stream().anyMatch(u -> u.getId().equals(2L)));
        assertTrue(result.getUsersDislikedNews().isEmpty());
        assertEquals(1, result.getEcoNewsComments().size());
    }

    @Test
    void convertWithEmptyCollections() {
        User author = User.builder().id(1L).name("A").build();
        EcoNews news = EcoNews.builder()
            .id(1L).title("T").text("C").creationDate(ZonedDateTime.now())
            .author(author).tags(Collections.emptyList())
            .usersLikedNews(Collections.emptySet())
            .usersDislikedNews(Collections.emptySet())
            .ecoNewsComments(Collections.emptyList())
            .build();
        EcoNewsVO result = mapper.convert(news);
        assertTrue(result.getTags().isEmpty());
        assertTrue(result.getUsersLikedNews().isEmpty());
        assertTrue(result.getUsersDislikedNews().isEmpty());
        assertTrue(result.getEcoNewsComments().isEmpty());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSource() {
        assertThrows(NullPointerException.class, () -> mapper.convert((EcoNews) null));
    }
}
