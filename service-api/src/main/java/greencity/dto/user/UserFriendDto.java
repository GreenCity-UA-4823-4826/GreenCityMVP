package greencity.dto.user;

import greencity.enums.friendship.FriendshipStatus;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class UserFriendDto {
    private Long id;
    private String name;
    private String profilePicturePath;
    private Double rating;
    private String city;
    private long mutualFriendsCount;
    private FriendshipStatus friendshipStatus;
    private boolean requestedByCurrentUser;
}