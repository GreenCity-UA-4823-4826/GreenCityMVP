package greencity.service;

import greencity.dto.user.UserFriendDto;
import greencity.dto.user.UserVO;
import greencity.entity.User;
import greencity.entity.friendship.Friendship;
import greencity.enums.friendship.FriendshipStatus;
import greencity.enums.NotificationType;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.mapping.UserFriendDtoMapper;
import greencity.repository.FriendshipRepo;
import greencity.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService{
    private final FriendshipRepo friendshipRepo;
    private final UserFriendDtoMapper userFriendDtoMapper;
    private final UserRepo userRepo;
    private final UserNotificationService userNotificationService;

    @Override
    @Transactional(readOnly = true)
    public Page<UserFriendDto> getFriends(UserVO userVO, Pageable pageable) {
        return getFriends(userVO.getId(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserFriendDto> getFriends(Long userId, Pageable pageable) {
        return friendshipRepo
            .findAllByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED, pageable)
            .map(friendship -> userFriendDtoMapper.toDto(
                friendship.getRequester().getId().equals(userId)
                    ? friendship.getReceiver()
                    : friendship.getRequester()));
    }

    @Override
    public long countOfUserFriends(UserVO userVO) {
        return friendshipRepo.countOfUserFriends(userVO.getId(), FriendshipStatus.ACCEPTED);
    }

    @Override
    public Page<UserFriendDto> searchFriends(UserVO userVO, String query, Boolean filterByCity, Boolean filterByFriendsOfFriends, Pageable pageable) {
        Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return friendshipRepo.searchUsers(userVO.getId(), query, filterByCity, filterByFriendsOfFriends, unsorted)
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

        try {
            friendshipRepo.save(friendship);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Friendship already exists between these users");
        }
        userNotificationService.createNotification(
            toUserVO(receiver),
            userVO,
            NotificationType.FRIEND_REQUEST,
            requester.getId(),
            requester.getName());
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
        userNotificationService.deleteNotification(
            NotificationType.FRIEND_REQUEST,
            receiverId,
            userVO.getId());
    }

    @Override
    public void removeFriend(UserVO userVO, Long friendId) {
        Friendship friendship = friendshipRepo
            .findFriendshipBetweenUsers(userVO.getId(), friendId)
            .orElseThrow(() -> new NotFoundException("Friendship not found"));

        if (friendship.getFriendshipStatus() != FriendshipStatus.ACCEPTED) {
            throw new BadRequestException("Only accepted friendships can be removed");
        }

        friendshipRepo.delete(friendship);
    }

    @Override
    public void acceptFriendRequest(UserVO userVO, Long requesterId) {
        Friendship friendship = getReceivedPendingRequest(userVO.getId(), requesterId);
        friendship.setFriendshipStatus(FriendshipStatus.ACCEPTED);
        friendshipRepo.save(friendship);
        userNotificationService.deleteNotification(
            NotificationType.FRIEND_REQUEST,
            userVO.getId(),
            requesterId);
    }

    @Override
    public void declineFriendRequest(UserVO userVO, Long requesterId) {
        friendshipRepo.delete(getReceivedPendingRequest(userVO.getId(), requesterId));
        userNotificationService.deleteNotification(
            NotificationType.FRIEND_REQUEST,
            userVO.getId(),
            requesterId);
    }

    private Friendship getReceivedPendingRequest(Long receiverId, Long requesterId) {
        Friendship friendship = friendshipRepo.findByRequesterIdAndReceiverId(requesterId, receiverId)
            .orElseThrow(() -> new NotFoundException("Friend request not found"));
        if (friendship.getFriendshipStatus() != FriendshipStatus.PENDING) {
            throw new BadRequestException("Only pending requests can be processed");
        }
        return friendship;
    }

    private UserVO toUserVO(User user) {
        return UserVO.builder()
            .id(user.getId())
            .name(user.getName())
            .build();
    }
}
