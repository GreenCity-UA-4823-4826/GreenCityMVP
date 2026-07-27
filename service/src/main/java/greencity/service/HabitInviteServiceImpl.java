package greencity.service;

import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableDto;
import greencity.dto.habitinvite.HabitInviteDto;
import greencity.dto.user.UserVO;
import greencity.entity.Habit;
import greencity.entity.HabitInvite;
import greencity.entity.HabitTranslation;
import greencity.entity.User;
import greencity.enums.HabitInviteStatus;
import greencity.event.HabitInviteNotificationEvent;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.repository.HabitAssignRepo;
import greencity.repository.HabitInviteRepo;
import greencity.repository.HabitRepo;
import greencity.repository.HabitTranslationRepo;
import greencity.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitInviteServiceImpl implements HabitInviteService {
    private final HabitInviteRepo habitInviteRepo;
    private final HabitRepo habitRepo;
    private final HabitAssignRepo habitAssignRepo;
    private final HabitTranslationRepo habitTranslationRepo;
    private final UserRepo userRepo;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public HabitInviteDto sendInvite(Long habitId, Long inviteeId, UserVO inviterVO) {
        if (inviterVO.getId().equals(inviteeId)) {
            throw new BadRequestException(ErrorMessage.CANNOT_INVITE_YOURSELF);
        }
        Habit habit = habitRepo.findById(habitId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));
        User invitee = userRepo.findById(inviteeId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + inviteeId));
        if (habitAssignRepo.findByHabitIdAndUserId(habitId, inviteeId).isPresent()) {
            throw new BadRequestException(ErrorMessage.INVITEE_ALREADY_HAS_HABIT_ASSIGNED);
        }

        LocalDateTime now = LocalDateTime.now();
        HabitInvite invite = habitInviteRepo
            .findByHabitIdAndInviterIdAndInviteeIdAndStatus(habitId, inviterVO.getId(), inviteeId,
                HabitInviteStatus.PENDING)
            .orElseGet(() -> HabitInvite.builder()
                .habit(habit)
                .inviter(userRepo.getReferenceById(inviterVO.getId()))
                .invitee(invitee)
                .status(HabitInviteStatus.PENDING)
                .createdDate(now)
                .build());
        invite.setUpdatedDate(now);
        HabitInvite saved = habitInviteRepo.save(invite);

        eventPublisher.publishEvent(HabitInviteNotificationEvent.builder()
            .invitee(modelMapper.map(invitee, UserVO.class))
            .inviter(inviterVO)
            .habitId(habitId)
            .habitName(findHabitName(habit))
            .active(true)
            .build());

        return toDto(saved);
    }

    @Override
    @Transactional
    public HabitInviteDto cancelInvite(Long inviteId, UserVO user) {
        HabitInvite invite = findInvite(inviteId);
        if (!invite.getInviter().getId().equals(user.getId())) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }
        transitionAwayFromPending(invite, HabitInviteStatus.CANCELLED, user);
        return toDto(invite);
    }

    @Override
    @Transactional
    public HabitInviteDto acceptInvite(Long inviteId, UserVO user) {
        HabitInvite invite = findInvite(inviteId);
        if (!invite.getInvitee().getId().equals(user.getId())) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }
        transitionAwayFromPending(invite, HabitInviteStatus.ACCEPTED, user);
        return toDto(invite);
    }

    @Override
    @Transactional
    public HabitInviteDto declineInvite(Long inviteId, UserVO user) {
        HabitInvite invite = findInvite(inviteId);
        if (!invite.getInvitee().getId().equals(user.getId())) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }
        transitionAwayFromPending(invite, HabitInviteStatus.DECLINED, user);
        return toDto(invite);
    }

    @Override
    public PageableDto<HabitInviteDto> getSentPendingInvites(UserVO inviter, Pageable pageable) {
        Page<HabitInvite> invites = habitInviteRepo.findAllByInviterIdAndStatusOrderByCreatedDateDesc(
            inviter.getId(), HabitInviteStatus.PENDING, pageable);
        List<HabitInviteDto> dtoList = invites.stream().map(this::toDto).toList();
        return new PageableDto<>(
            dtoList,
            invites.getTotalElements(),
            invites.getPageable().getPageNumber(),
            invites.getTotalPages());
    }

    private HabitInvite findInvite(Long inviteId) {
        return habitInviteRepo.findById(inviteId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_INVITE_NOT_FOUND_BY_ID + inviteId));
    }

    private void transitionAwayFromPending(HabitInvite invite, HabitInviteStatus newStatus, UserVO actingUser) {
        if (invite.getStatus() != HabitInviteStatus.PENDING) {
            throw new BadRequestException(ErrorMessage.HABIT_INVITE_ALREADY_HANDLED);
        }
        invite.setStatus(newStatus);
        invite.setUpdatedDate(LocalDateTime.now());
        habitInviteRepo.save(invite);

        Long inviterId = invite.getInviter().getId();
        Long inviteeId = invite.getInvitee().getId();
        UserVO inviterVO = inviterId.equals(actingUser.getId()) ? actingUser : loadUserVO(inviterId);
        UserVO inviteeVO = inviteeId.equals(actingUser.getId()) ? actingUser : loadUserVO(inviteeId);

        eventPublisher.publishEvent(HabitInviteNotificationEvent.builder()
            .invitee(inviteeVO)
            .inviter(inviterVO)
            .habitId(invite.getHabit().getId())
            .habitName(findHabitName(invite.getHabit()))
            .active(false)
            .build());
    }

    private UserVO loadUserVO(Long userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + userId));
        return modelMapper.map(user, UserVO.class);
    }

    private String findHabitName(Habit habit) {
        return habitTranslationRepo.findByHabitAndLanguageCode(habit, AppConstant.DEFAULT_LANGUAGE_CODE)
            .map(HabitTranslation::getName)
            .orElse(null);
    }

    private HabitInviteDto toDto(HabitInvite invite) {
        return HabitInviteDto.builder()
            .id(invite.getId())
            .habitId(invite.getHabit().getId())
            .habitName(findHabitName(invite.getHabit()))
            .inviterId(invite.getInviter().getId())
            .inviteeId(invite.getInvitee().getId())
            .status(invite.getStatus())
            .createdDate(invite.getCreatedDate())
            .updatedDate(invite.getUpdatedDate())
            .build();
    }
}
