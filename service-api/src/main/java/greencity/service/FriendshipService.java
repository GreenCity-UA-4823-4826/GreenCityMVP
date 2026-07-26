package greencity.service;

import greencity.dto.user.UserFriendDto;
import greencity.dto.user.UserVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FriendshipService {
    /**
     * Returns accepted friends of the current user.
     *
     * @param userVO   current authorized user.
     * @param pageable pagination parameters.
     * @return page of accepted friends.
     */
    Page<UserFriendDto> getFriends(UserVO userVO, Pageable pageable);

    /**
     * Returns accepted friends of the requested user.
     *
     * @param userId   id of the profile owner.
     * @param pageable pagination parameters.
     * @return page of accepted friends.
     */
    Page<UserFriendDto> getFriends(Long userId, Pageable pageable);

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
     * @param userVO                  - current authorized user.
     * @param query                   - search string to filter by name.
     * @param filterByCity            - if true, returns only users from the same city.
     * @param filterByFriendsOfFriends - if true, returns only friends of current user's friends.
     * @param pageable                - pagination parameters.
     * @return {@link Page} of {@link UserFriendDto}.
     */
    Page<UserFriendDto> searchFriends(UserVO userVO,
                                      String query,
                                      Boolean filterByCity,
                                      Boolean filterByFriendsOfFriends,
                                      Pageable pageable);

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

    /**
     * Removes an accepted friendship of the current user.
     *
     * @param userVO  current authorized user.
     * @param friendId id of the friend to remove.
     */
    void removeFriend(UserVO userVO, Long friendId);

    /**
     * Accepts a pending friend request received by the current user.
     *
     * @param userVO      current authorized user.
     * @param requesterId id of the user who sent the request.
     */
    void acceptFriendRequest(UserVO userVO, Long requesterId);

    /**
     * Declines a pending friend request received by the current user.
     *
     * @param userVO      current authorized user.
     * @param requesterId id of the user who sent the request.
     */
    void declineFriendRequest(UserVO userVO, Long requesterId);
}
