package greencity.mapping;

import greencity.dto.user.UserFriendDto;
import greencity.entity.User;
import greencity.enums.friendship.FriendshipStatus;
import org.springframework.stereotype.Component;

@Component
public class UserFriendDtoMapper {
    public UserFriendDto toDto(User user) {
        return UserFriendDto.builder()
            .id(user.getId())
            .name(user.getName())
            .profilePicturePath(user.getProfilePicturePath())
            .rating(user.getRating())
            .city(user.getCity())
            .friendshipStatus(FriendshipStatus.ACCEPTED)
            .build();
    }

    public UserFriendDto toDto(Object[] row) {
        return UserFriendDto.builder()
                .id(((Number) row[0]).longValue())
                .name((String) row[1])
                .profilePicturePath((String) row[2])
                .rating(row[3] != null ? ((Number) row[3]).doubleValue() : null)
                .city((String) row[4])
                .mutualFriendsCount(row[5] != null ? ((Number) row[5]).longValue() : 0L)
                .friendshipStatus(row[6] != null ? FriendshipStatus.valueOf((String) row[6]) : null)
                .requestedByCurrentUser(Boolean.TRUE.equals(row[7]))
                .build();
    }
}