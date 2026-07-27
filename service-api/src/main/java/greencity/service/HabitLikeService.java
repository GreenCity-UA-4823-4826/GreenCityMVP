package greencity.service;

import greencity.dto.habitlike.HabitLikeDtoResponse;
import greencity.dto.user.UserVO;

public interface HabitLikeService {
    /**
     * Method to like a habit. Idempotent - liking an already-liked habit has no
     * effect. Publishes a notification event for the habit's owner, unless the
     * owner is the one liking their own habit, or the habit has no single owner (a
     * default, non-custom habit).
     *
     * @param habitId id of the habit to like.
     * @param user    {@link UserVO} that likes the habit.
     * @return {@link HabitLikeDtoResponse} with the current like state and count.
     */
    HabitLikeDtoResponse like(Long habitId, UserVO user);

    /**
     * Method to remove a like from a habit. Idempotent - unliking a habit that
     * wasn't liked has no effect. Removes the liker from the corresponding unread
     * notification, if any.
     *
     * @param habitId id of the habit to unlike.
     * @param user    {@link UserVO} that unlikes the habit.
     * @return {@link HabitLikeDtoResponse} with the current like state and count.
     */
    HabitLikeDtoResponse unlike(Long habitId, UserVO user);
}
