package greencity.repository;

import greencity.entity.HabitLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HabitLikeRepo extends JpaRepository<HabitLike, Long> {
    Optional<HabitLike> findByHabitIdAndUserId(Long habitId, Long userId);

    boolean existsByHabitIdAndUserId(Long habitId, Long userId);

    long countByHabitId(Long habitId);
}
