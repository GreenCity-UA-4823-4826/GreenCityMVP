package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.eventcomment.AddEventCommentDtoRequest;
import greencity.dto.eventcomment.AddEventCommentDtoResponse;
import greencity.dto.eventcomment.EventCommentDto;
import greencity.dto.user.UserVO;
import org.springframework.data.domain.Pageable;

public interface EventCommentService {
    /**
     * Method to save a comment to an event. Publishes a notification event for the
     * event organizer, unless the organizer is the one leaving the comment.
     *
     * @param eventId                   id of the event to which we save the
     *                                  comment.
     * @param addEventCommentDtoRequest dto with comment text.
     * @param user                      {@link UserVO} that saves the comment.
     * @return {@link AddEventCommentDtoResponse} instance.
     */
    AddEventCommentDtoResponse save(Long eventId, AddEventCommentDtoRequest addEventCommentDtoRequest, UserVO user);

    /**
     * Method returns all comments to certain event specified by eventId.
     *
     * @param pageable pageable configuration.
     * @param eventId  specifies event to which we search for comments.
     * @return page of comments to certain event specified by eventId.
     */
    PageableDto<EventCommentDto> findAllComments(Pageable pageable, Long eventId);

    /**
     * Method to delete a comment. Only the comment's author can delete it.
     *
     * @param id   id of the comment to delete.
     * @param user current {@link UserVO} that wants to delete.
     */
    void deleteById(Long id, UserVO user);
}
