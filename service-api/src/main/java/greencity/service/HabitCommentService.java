package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.habitcomment.AddHabitCommentDtoRequest;
import greencity.dto.habitcomment.AddHabitCommentDtoResponse;
import greencity.dto.habitcomment.HabitCommentDto;
import greencity.dto.user.UserVO;
import org.springframework.data.domain.Pageable;

public interface HabitCommentService {
    /**
     * Method to save a comment to a habit. Publishes a notification event for the
     * habit's owner, unless the owner is the one leaving the comment, or the habit
     * has no single owner (a default, non-custom habit).
     *
     * @param habitId                   id of the habit to which we save the
     *                                  comment.
     * @param addHabitCommentDtoRequest dto with comment text.
     * @param user                      {@link UserVO} that saves the comment.
     * @return {@link AddHabitCommentDtoResponse} instance.
     */
    AddHabitCommentDtoResponse save(Long habitId, AddHabitCommentDtoRequest addHabitCommentDtoRequest, UserVO user);

    /**
     * Method returns all comments to certain habit specified by habitId.
     *
     * @param pageable pageable configuration.
     * @param habitId  specifies habit to which we search for comments.
     * @return page of comments to certain habit specified by habitId.
     */
    PageableDto<HabitCommentDto> findAllComments(Pageable pageable, Long habitId);

    /**
     * Method to delete a comment. The comment can be deleted by its author or an
     * administrator.
     *
     * @param id   id of the comment to delete.
     * @param user current {@link UserVO} that wants to delete.
     */
    void deleteById(Long id, UserVO user);
}
