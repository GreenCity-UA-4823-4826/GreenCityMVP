package greencity.repository;

import greencity.entity.event.EventAttendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventAttendanceRepo extends JpaRepository<EventAttendance, Long> {
    Page<EventAttendance> findByUserId(Long userId, Pageable pageable);

    Optional<EventAttendance> findByEventIdAndUserId(Long eventId, Long userId);
}
