package greencity.service;

import greencity.dto.eventlike.EventLikeDtoResponse;
import greencity.dto.user.UserVO;

public interface EventLikeService {
    /**
     * Method to like an event. Idempotent - liking an already-liked event has no
     * effect. Publishes a notification event for the event organizer, unless the
     * organizer is the one liking their own event.
     *
     * @param eventId id of the event to like.
     * @param user    {@link UserVO} that likes the event.
     * @return {@link EventLikeDtoResponse} with the current like state and count.
     */
    EventLikeDtoResponse like(Long eventId, UserVO user);

    /**
     * Method to remove a like from an event. Idempotent - unliking an event that
     * wasn't liked has no effect. Removes the liker from the corresponding unread
     * notification, if any.
     *
     * @param eventId id of the event to unlike.
     * @param user    {@link UserVO} that unlikes the event.
     * @return {@link EventLikeDtoResponse} with the current like state and count.
     */
    EventLikeDtoResponse unlike(Long eventId, UserVO user);
}
