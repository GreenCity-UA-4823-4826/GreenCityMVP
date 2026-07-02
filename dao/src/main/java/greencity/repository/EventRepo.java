package greencity.repository;

import greencity.entity.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface EventRepo extends JpaRepository<Event, Long> {
    Page<Event> findByOrganizerId(Long organizerId, Pageable pageable);
}
