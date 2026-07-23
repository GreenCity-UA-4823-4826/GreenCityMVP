package greencity.service;

import greencity.dto.user.UserFriendDto;
import greencity.dto.user.UserVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FriendshipService {
    /**
     * Method for counting friends of the current user.
     *
     * @param userVO - current authorized user.
     * @return number of friends as {@code long}.
     */
    long countOfUserFriends(UserVO userVO);

    /**
     * Method for searching users by name.
     *
     * @param userVO   - current authorized user.
     * @param query    - search string to filter by name.
     * @param pageable - pagination parameters.
     * @return {@link Page} of {@link UserFriendDto}.
     */
    Page<UserFriendDto> searchFriends(UserVO userVO, String query, Pageable pageable);

    /**
     * Method for sending a friend request to another user.
     *
     * @param userVO     - current authorized user.
     * @param receiverId - id of the user to send request to.
     */
    void addFriend(UserVO userVO, Long receiverId);

    /**
     * Method for cancelling a friend request sent by current user.
     *
     * @param userVO     - current authorized user.
     * @param receiverId - id of the user to cancel request for.
     */
    void cancelFriendRequest(UserVO userVO, Long receiverId);
}
