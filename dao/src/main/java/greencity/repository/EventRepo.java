package greencity.repository;

import greencity.entity.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepo extends JpaRepository<Event, Long> {
    List<Event> findByOrganizerId(Long organizerId);
}