package greencity.repository;

import greencity.entity.event.EventComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventCommentRepo extends JpaRepository<EventComment, Long> {
    Page<EventComment> findAllByEventIdOrderByCreatedDateDesc(Long eventId, Pageable pageable);

    boolean existsByIdAndUserId(Long id, Long userId);
}
