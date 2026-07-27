package greencity.repository;

import greencity.entity.HabitComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HabitCommentRepo extends JpaRepository<HabitComment, Long> {
    @Query("SELECT c FROM HabitComment c JOIN FETCH c.user WHERE c.habit.id = :habitId "
        + "ORDER BY c.createdDate DESC")
    Page<HabitComment> findAllByHabitIdOrderByCreatedDateDesc(Long habitId, Pageable pageable);

    boolean existsByIdAndUserId(Long id, Long userId);
}
