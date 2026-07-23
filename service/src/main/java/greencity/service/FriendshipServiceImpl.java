package greencity.service;

import greencity.dto.user.UserVO;
import greencity.enums.friendship.FriendshipStatus;
import greencity.repository.FriendshipRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService{
    private final FriendshipRepo friendshipRepo;

    @Override
    public long countOfUserFriends(UserVO userVO) {
        return friendshipRepo.countOfUserFriends(userVO.getId(), FriendshipStatus.ACCEPTED);
    }
}
