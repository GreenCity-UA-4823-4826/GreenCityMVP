package greencity.service;

import greencity.dto.user.UserVO;

public interface FriendshipService {
    long countOfUserFriends(UserVO userVO);
}
