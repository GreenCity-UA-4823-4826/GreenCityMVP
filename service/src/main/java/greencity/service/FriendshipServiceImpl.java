package greencity.service;

import greencity.dto.user.UserFriendDto;
import greencity.dto.user.UserVO;
import greencity.entity.User;
import greencity.entity.friendship.Friendship;
import greencity.enums.friendship.FriendshipStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.mapping.UserFriendDtoMapper;
import greencity.repository.FriendshipRepo;
import greencity.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService{
    private final FriendshipRepo friendshipRepo;
    private final UserFriendDtoMapper userFriendDtoMapper;
    private final UserRepo userRepo;

    @Override
    public long countOfUserFriends(UserVO userVO) {
        return friendshipRepo.countOfUserFriends(userVO.getId(), FriendshipStatus.ACCEPTED);
    }

    @Override
    public Page<UserFriendDto> searchFriends(UserVO userVO, String query, Pageable pageable) {
        Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return friendshipRepo.searchUsers(userVO.getId(), query, unsorted)
                .map(userFriendDtoMapper::toDto);
    }

    @Override
    public void addFriend(UserVO userVO, Long receiverId) {
        if (userVO.getId().equals(receiverId)) {
            throw new BadRequestException("User cannot send a friend request to themselves");
        }

        friendshipRepo.findFriendshipBetweenUsers(userVO.getId(), receiverId)
                .ifPresent(f -> {
                    throw new BadRequestException("Friendship already exists between these users");
                });

        User requester = userRepo.findById(userVO.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        User receiver = userRepo.findById(receiverId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Friendship friendship = new Friendship();
        friendship.setRequester(requester);
        friendship.setReceiver(receiver);
        friendship.setFriendshipStatus(FriendshipStatus.PENDING);

        friendshipRepo.save(friendship);
    }

    @Override
    public void cancelFriendRequest(UserVO userVO, Long receiverId) {
        Friendship friendship = friendshipRepo
                .findByRequesterIdAndReceiverId(userVO.getId(), receiverId)
                .orElseThrow(() -> new NotFoundException("Friend request not found"));

        if (friendship.getFriendshipStatus() != FriendshipStatus.PENDING) {
            throw new BadRequestException("Only pending requests can be cancelled");
        }

        friendshipRepo.delete(friendship);
    }
}
