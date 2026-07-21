package greencity.service;

import greencity.dto.user.UserVO;

public interface FriendshipService {
    /**
     * Method for counting friends of the current user.
     *
     * @param userVO - current authorized user.
     * @return number of friends as {@code long}.
     */
    long countOfUserFriends(UserVO userVO);
}
