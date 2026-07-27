package greencity.repository;

import greencity.entity.event.EventAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventAttendanceRepo extends JpaRepository<EventAttendance, Long> {
    List<EventAttendance> findByUserId(Long userId);

    Optional<EventAttendance> findByEventIdAndUserId(Long eventId, Long userId);

    List<EventAttendance> findByEventId(Long eventId);
}