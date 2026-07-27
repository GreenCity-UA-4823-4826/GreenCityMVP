package greencity.mapping;

import greencity.dto.econewscomment.EcoNewsCommentVO;
import greencity.entity.EcoNews;
import greencity.entity.EcoNewsComment;
import greencity.entity.User;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EcoNewsCommentVOMapperTest {
    private final EcoNewsCommentVOMapper mapper = new EcoNewsCommentVOMapper();

    private User createUser(Long id, String name) {
        return User.builder().id(id).name(name)
            .role(Role.ROLE_USER).userStatus(UserStatus.ACTIVATED).build();
    }

    private EcoNews createEcoNews() {
        return EcoNews.builder().id(1L).build();
    }

    @Test
    void convertWithoutParentComment() {
        LocalDateTime now = LocalDateTime.now();
        User user = createUser(1L, "User");
        EcoNewsComment comment = EcoNewsComment.builder()
            .id(1L).text("text").createdDate(now).modifiedDate(now)
            .deleted(false).currentUserLiked(true).user(user).parentComment(null)
            .usersLiked(Collections.emptySet()).ecoNews(createEcoNews())
            .build();
        EcoNewsCommentVO result = mapper.convert(comment);
        assertEquals(1L, result.getId());
        assertEquals("text", result.getText());
        assertEquals(now, result.getCreatedDate());
        assertEquals(now, result.getModifiedDate());
        assertFalse(result.isDeleted());
        assertTrue(result.isCurrentUserLiked());
        assertNull(result.getParentComment());
        assertEquals(user.getId(), result.getUser().getId());
        assertEquals(user.getName(), result.getUser().getName());
        assertEquals(user.getRole(), result.getUser().getRole());
        assertEquals(1L, result.getEcoNews().getId());
        assertTrue(result.getUsersLiked().isEmpty());
    }

    @Test
    void convertWithParentComment() {
        LocalDateTime now = LocalDateTime.now();
        User user = createUser(1L, "User");
        User parentUser = createUser(2L, "Parent");
        EcoNewsComment parentComment = EcoNewsComment.builder()
            .id(2L).text("parent").createdDate(now).modifiedDate(now)
            .deleted(false).currentUserLiked(false).user(parentUser).parentComment(null)
            .usersLiked(Collections.emptySet()).ecoNews(createEcoNews())
            .build();
        EcoNewsComment comment = EcoNewsComment.builder()
            .id(1L).text("child").createdDate(now).modifiedDate(now)
            .deleted(false).currentUserLiked(false).user(user).parentComment(parentComment)
            .usersLiked(Collections.emptySet()).ecoNews(createEcoNews())
            .build();
        EcoNewsCommentVO result = mapper.convert(comment);
        assertNotNull(result.getParentComment());
        assertEquals(2L, result.getParentComment().getId());
        assertEquals("parent", result.getParentComment().getText());
        assertNull(result.getParentComment().getParentComment());
    }

    @Test
    void convertWithUsersLiked() {
        User user = createUser(1L, "User");
        User likedUser = createUser(2L, "Liked");
        EcoNewsComment comment = EcoNewsComment.builder()
            .id(1L).text("text").createdDate(LocalDateTime.now()).modifiedDate(LocalDateTime.now())
            .deleted(false).currentUserLiked(false).user(user).parentComment(null)
            .usersLiked(new HashSet<>(Collections.singletonList(likedUser)))
            .ecoNews(createEcoNews())
            .build();
        EcoNewsCommentVO result = mapper.convert(comment);
        assertEquals(1, result.getUsersLiked().size());
        assertTrue(result.getUsersLiked().stream().anyMatch(u -> u.getId().equals(2L)));
    }

    @Test
    void convertWithNullSource() {
        assertThrows(NullPointerException.class, () -> mapper.convert((EcoNewsComment) null));
    }
}
