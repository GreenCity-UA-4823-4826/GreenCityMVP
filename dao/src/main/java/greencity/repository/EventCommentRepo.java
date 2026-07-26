package greencity.repository;

import greencity.entity.event.EventComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EventCommentRepo extends JpaRepository<EventComment, Long> {
    @Query("SELECT c FROM EventComment c JOIN FETCH c.user WHERE c.event.id = :eventId "
        + "ORDER BY c.createdDate DESC, c.id DESC")
    Page<EventComment> findAllByEventIdOrderByCreatedDateDesc(Long eventId, Pageable pageable);

    boolean existsByIdAndUserId(Long id, Long userId);
}
