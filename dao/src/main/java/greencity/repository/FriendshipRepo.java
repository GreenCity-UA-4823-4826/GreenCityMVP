package greencity.repository;

import greencity.entity.friendship.Friendship;
import greencity.enums.friendship.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepo extends JpaRepository<Friendship, Long> {
    /**
     * Count friends of the given user with the given status.
     *
     * @param userId - id of the user.
     * @param status - {@link FriendshipStatus} to filter by.
     * @return number of friendships as {@code long}.
     */
    @Query("SELECT COUNT(f) FROM Friendship f " +
            "WHERE (f.receiver.id = :userId OR f.requester.id = :userId) AND f.friendshipStatus = :status")
    long countOfUserFriends(@Param("userId") Long userId, @Param("status")FriendshipStatus status);
}
