package greencity.mapping;

import greencity.dto.econewscomment.EcoNewsCommentDto;
import greencity.entity.EcoNewsComment;
import greencity.entity.User;
import greencity.enums.CommentStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EcoNewsCommentDtoMapperTest {
    private final EcoNewsCommentDtoMapper mapper = new EcoNewsCommentDtoMapper();

    private User createUser() {
        return User.builder().id(1L).name("User").profilePicturePath("pic.jpg").build();
    }

    @Test
    void convertWithStatusOriginal() {
        LocalDateTime now = LocalDateTime.now();
        User user = createUser();
        EcoNewsComment comment = EcoNewsComment.builder()
            .id(1L).text("text").createdDate(now).modifiedDate(now).deleted(false)
            .currentUserLiked(true).user(user).usersLiked(Collections.emptySet())
            .build();
        EcoNewsCommentDto result = mapper.convert(comment);
        assertEquals(1L, result.getId());
        assertEquals(now, result.getModifiedDate());
        assertEquals(CommentStatus.ORIGINAL, result.getStatus());
        assertEquals("text", result.getText());
        assertEquals(0, result.getLikes());
        assertTrue(result.isCurrentUserLiked());
        assertEquals(user.getId(), result.getAuthor().getId());
        assertEquals(user.getName(), result.getAuthor().getName());
        assertEquals(user.getProfilePicturePath(), result.getAuthor().getUserProfilePicturePath());
    }

    @Test
    void convertWithStatusEdited() {
        LocalDateTime created = LocalDateTime.now().minusHours(2);
        LocalDateTime modified = LocalDateTime.now();
        User user = createUser();
        EcoNewsComment comment = EcoNewsComment.builder()
            .id(2L).text("edited").createdDate(created).modifiedDate(modified).deleted(false)
            .currentUserLiked(false).user(user)
            .usersLiked(new HashSet<>(Collections.singletonList(user)))
            .build();
        EcoNewsCommentDto result = mapper.convert(comment);
        assertEquals(CommentStatus.EDITED, result.getStatus());
        assertEquals("edited", result.getText());
        assertEquals(1, result.getLikes());
    }

    @Test
    void convertWithStatusDeleted() {
        User user = createUser();
        EcoNewsComment comment = EcoNewsComment.builder()
            .id(3L).text("deleted").createdDate(LocalDateTime.now()).modifiedDate(LocalDateTime.now())
            .deleted(true).currentUserLiked(false).user(user).usersLiked(Collections.emptySet())
            .build();
        EcoNewsCommentDto result = mapper.convert(comment);
        assertEquals(CommentStatus.DELETED, result.getStatus());
        assertNull(result.getText());
        assertNull(result.getAuthor());
        assertEquals(0, result.getLikes());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void convertWithNullSource() {
        assertThrows(NullPointerException.class, () -> mapper.convert((EcoNewsComment) null));
    }
}
