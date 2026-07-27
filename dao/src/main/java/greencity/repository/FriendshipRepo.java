package greencity.repository;

import greencity.entity.friendship.Friendship;
import greencity.enums.friendship.FriendshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendshipRepo extends JpaRepository<Friendship, Long> {
    @Query("""
        SELECT f FROM Friendship f
        WHERE (f.requester.id = :userId OR f.receiver.id = :userId)
          AND f.friendshipStatus = :status
        ORDER BY f.createdDate DESC
        """)
    Page<Friendship> findAllByUserIdAndStatus(
        @Param("userId") Long userId,
        @Param("status") FriendshipStatus status,
        Pageable pageable);

    /**
     * Count friends of the given user with the given status.
     *
     * @param userId - id of the user.
     * @param status - {@link FriendshipStatus} to filter by.
     * @return number of friendships as {@code long}.
     */
    @Query("SELECT COUNT(f) FROM Friendship f " +
            "WHERE (f.receiver.id = :userId OR f.requester.id = :userId) AND f.friendshipStatus = :status")
    long countOfUserFriends(@Param("userId") Long userId, @Param("status") FriendshipStatus status);

    /**
     * Find friendship between two users where current user is requester.
     *
     * @param requesterId - id of the user who sent the request.
     * @param receiverId  - id of the user who received the request.
     * @return {@link Optional} of {@link Friendship}.
     */
    Optional<Friendship> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);

    /**
     * Find friendship between two users regardless of who sent the request.
     *
     * @param userId1 - id of the first user.
     * @param userId2 - id of the second user.
     * @return {@link Optional} of {@link Friendship}.
     */
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester.id = :userId1 AND f.receiver.id = :userId2) OR " +
            "(f.requester.id = :userId2 AND f.receiver.id = :userId1)")
    Optional<Friendship> findFriendshipBetweenUsers(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);

    /**
     * Search users by name for the current user.
     * Excludes current user and already accepted friends.
     * Returns mutual friends count, friendship status and
     * whether current user sent the request.
     * Optionally filters by city and/or friends of friends.
     *
     * @param currentUserId           - id of the current user.
     * @param query                   - search string to filter by name.
     * @param filterByCity            - if true, returns only users from the same city.
     * @param filterByFriendsOfFriends - if true, returns only friends of current user's friends.
     * @param pageable                - pagination parameters.
     * @return {@link Page} of {@link Object[]} with user data.
     */
    @Query(value = """
    SELECT
        u.id,
        u.name,
        u.profile_picture AS profilePicturePath,
        u.rating,
        u.city,
        (SELECT COUNT(*)
         FROM friendships mf
         WHERE mf.friendship_status = 'ACCEPTED'
           AND ((mf.requester_id = u.id AND mf.receiver_id IN (
                   SELECT CASE WHEN f2.requester_id = :currentUserId
                               THEN f2.receiver_id ELSE f2.requester_id END
                   FROM friendships f2
                   WHERE (f2.requester_id = :currentUserId OR f2.receiver_id = :currentUserId)
                     AND f2.friendship_status = 'ACCEPTED'))
             OR (mf.receiver_id = u.id AND mf.requester_id IN (
                   SELECT CASE WHEN f2.requester_id = :currentUserId
                               THEN f2.receiver_id ELSE f2.requester_id END
                   FROM friendships f2
                   WHERE (f2.requester_id = :currentUserId OR f2.receiver_id = :currentUserId)
                     AND f2.friendship_status = 'ACCEPTED')))
        ) AS mutualFriendsCount,
        (SELECT f.friendship_status
         FROM friendships f
         WHERE (f.requester_id = :currentUserId AND f.receiver_id = u.id)
            OR (f.receiver_id = :currentUserId AND f.requester_id = u.id)
         LIMIT 1) AS friendshipStatus,
        CASE WHEN EXISTS (
            SELECT 1 FROM friendships f
            WHERE f.requester_id = :currentUserId AND f.receiver_id = u.id
        ) THEN true ELSE false END AS requestedByCurrentUser
    FROM users u
    WHERE u.id != :currentUserId
      AND NOT EXISTS (
          SELECT 1 FROM friendships f
          WHERE ((f.requester_id = :currentUserId AND f.receiver_id = u.id)
              OR (f.receiver_id = :currentUserId AND f.requester_id = u.id))
            AND f.friendship_status = 'ACCEPTED'
      )
      AND LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))
      AND (:filterByCity = false OR u.city = (
          SELECT city FROM users WHERE id = :currentUserId
      ))
      AND (:filterByFriendsOfFriends = false OR EXISTS (
          SELECT 1 FROM friendships f3
          WHERE f3.friendship_status = 'ACCEPTED'
            AND ((f3.requester_id = u.id AND f3.receiver_id IN (
                    SELECT CASE WHEN f4.requester_id = :currentUserId
                                THEN f4.receiver_id ELSE f4.requester_id END
                    FROM friendships f4
                    WHERE (f4.requester_id = :currentUserId OR f4.receiver_id = :currentUserId)
                      AND f4.friendship_status = 'ACCEPTED'))
              OR (f3.receiver_id = u.id AND f3.requester_id IN (
                    SELECT CASE WHEN f4.requester_id = :currentUserId
                                THEN f4.receiver_id ELSE f4.requester_id END
                    FROM friendships f4
                    WHERE (f4.requester_id = :currentUserId OR f4.receiver_id = :currentUserId)
                      AND f4.friendship_status = 'ACCEPTED')))
      ))
            ORDER BY
      CASE
        WHEN LOWER(u.name) LIKE LOWER(CONCAT(:query, '%')) THEN 0
        ELSE 1
      END,
      u.name,
      u.id
    """,
            countQuery = """
    SELECT COUNT(*) FROM users u
    WHERE u.id != :currentUserId
      AND NOT EXISTS (
          SELECT 1 FROM friendships f
          WHERE ((f.requester_id = :currentUserId AND f.receiver_id = u.id)
              OR (f.receiver_id = :currentUserId AND f.requester_id = u.id))
            AND f.friendship_status = 'ACCEPTED'
      )
      AND LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))
      AND (:filterByCity = false OR u.city = (
          SELECT city FROM users WHERE id = :currentUserId
      ))
      AND (:filterByFriendsOfFriends = false OR EXISTS (
          SELECT 1 FROM friendships f3
          WHERE f3.friendship_status = 'ACCEPTED'
            AND ((f3.requester_id = u.id AND f3.receiver_id IN (
                    SELECT CASE WHEN f4.requester_id = :currentUserId
                                THEN f4.receiver_id ELSE f4.requester_id END
                    FROM friendships f4
                    WHERE (f4.requester_id = :currentUserId OR f4.receiver_id = :currentUserId)
                      AND f4.friendship_status = 'ACCEPTED'))
              OR (f3.receiver_id = u.id AND f3.requester_id IN (
                    SELECT CASE WHEN f4.requester_id = :currentUserId
                                THEN f4.receiver_id ELSE f4.requester_id END
                    FROM friendships f4
                    WHERE (f4.requester_id = :currentUserId OR f4.receiver_id = :currentUserId)
                      AND f4.friendship_status = 'ACCEPTED')))
      ))
    """,
            nativeQuery = true)
    Page<Object[]> searchUsers(
            @Param("currentUserId") Long currentUserId,
            @Param("query") String query,
            @Param("filterByCity") Boolean filterByCity,
            @Param("filterByFriendsOfFriends") Boolean filterByFriendsOfFriends,
            Pageable pageable);
}
