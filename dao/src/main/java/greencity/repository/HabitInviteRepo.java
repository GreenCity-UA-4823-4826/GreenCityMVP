package greencity.repository;

import greencity.entity.HabitInvite;
import greencity.enums.HabitInviteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HabitInviteRepo extends JpaRepository<HabitInvite, Long> {
    Optional<HabitInvite> findByHabitIdAndInviterIdAndInviteeIdAndStatus(Long habitId, Long inviterId,
        Long inviteeId, HabitInviteStatus status);

    boolean existsByIdAndInviterId(Long id, Long inviterId);

    boolean existsByIdAndInviteeId(Long id, Long inviteeId);

    Page<HabitInvite> findAllByInviterIdAndStatusOrderByCreatedDateDesc(Long inviterId, HabitInviteStatus status,
        Pageable pageable);
}
