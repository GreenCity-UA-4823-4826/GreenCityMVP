package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.habitinvite.HabitInviteDto;
import greencity.dto.user.UserVO;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.getUserVO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabitInviteServiceImplTest {
    @Mock
    private HabitInviteRepo habitInviteRepo;
    @Mock
    private HabitRepo habitRepo;
    @Mock
    private HabitAssignRepo habitAssignRepo;
    @Mock
    private HabitTranslationRepo habitTranslationRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private HabitInviteServiceImpl habitInviteService;

    private Habit getHabit() {
        return Habit.builder().id(1L).build();
    }

    private User buildUser(long id) {
        User user = getUser();
        user.setId(id);
        return user;
    }

    private HabitInvite getInvite(HabitInviteStatus status) {
        return HabitInvite.builder()
            .id(1L)
            .habit(getHabit())
            .inviter(buildUser(1L))
            .invitee(buildUser(2L))
            .status(status)
            .createdDate(LocalDateTime.now())
            .updatedDate(LocalDateTime.now())
            .build();
    }

    @Test
    void sendInvite_ValidRequest_CreatesNewInviteAndPublishesNotificationEvent() {
        UserVO inviterVO = getUserVO();
        User invitee = buildUser(2L);
        HabitTranslation translation = HabitTranslation.builder().name("Drink water").build();

        when(habitRepo.findById(1L)).thenReturn(Optional.of(getHabit()));
        when(userRepo.findById(2L)).thenReturn(Optional.of(invitee));
        when(habitAssignRepo.findByHabitIdAndUserId(1L, 2L)).thenReturn(Optional.empty());
        when(habitInviteRepo.findByHabitIdAndInviterIdAndInviteeIdAndStatus(1L, 1L, 2L, HabitInviteStatus.PENDING))
            .thenReturn(Optional.empty());
        when(userRepo.getReferenceById(1L)).thenReturn(buildUser(1L));
        when(habitInviteRepo.save(any(HabitInvite.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(habitTranslationRepo.findByHabitAndLanguageCode(any(Habit.class), org.mockito.ArgumentMatchers.eq("en")))
            .thenReturn(Optional.of(translation));

        HabitInviteDto dto = habitInviteService.sendInvite(1L, 2L, inviterVO);

        assertEquals(1L, dto.getHabitId());
        assertEquals(HabitInviteStatus.PENDING, dto.getStatus());
        assertEquals("Drink water", dto.getHabitName());

        ArgumentCaptor<HabitInviteNotificationEvent> captor =
            ArgumentCaptor.forClass(HabitInviteNotificationEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(1L, captor.getValue().getHabitId());
        assertEquals(true, captor.getValue().isActive());
    }

    @Test
    void sendInvite_InviterInvitesThemselves_ThrowsBadRequestException() {
        UserVO inviterVO = getUserVO();

        assertThrows(BadRequestException.class,
            () -> habitInviteService.sendInvite(1L, inviterVO.getId(), inviterVO));
        verifyNoInteractions(habitRepo);
    }

    @Test
    void sendInvite_HabitNotFound_ThrowsNotFoundException() {
        when(habitRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> habitInviteService.sendInvite(1L, 2L, getUserVO()));
        verify(habitInviteRepo, never()).save(any(HabitInvite.class));
    }

    @Test
    void sendInvite_InviteeNotFound_ThrowsNotFoundException() {
        when(habitRepo.findById(1L)).thenReturn(Optional.of(getHabit()));
        when(userRepo.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> habitInviteService.sendInvite(1L, 2L, getUserVO()));
        verify(habitInviteRepo, never()).save(any(HabitInvite.class));
    }

    @Test
    void sendInvite_InviteeAlreadyHasHabitAssigned_ThrowsBadRequestException() {
        when(habitRepo.findById(1L)).thenReturn(Optional.of(getHabit()));
        when(userRepo.findById(2L)).thenReturn(Optional.of(buildUser(2L)));
        when(habitAssignRepo.findByHabitIdAndUserId(1L, 2L)).thenReturn(Optional.of(new HabitAssign()));

        assertThrows(BadRequestException.class, () -> habitInviteService.sendInvite(1L, 2L, getUserVO()));
        verify(habitInviteRepo, never()).save(any(HabitInvite.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void sendInvite_PendingInviteAlreadyExists_RefreshesExistingInviteInsteadOfCreatingNew() {
        UserVO inviterVO = getUserVO();
        HabitInvite existing = getInvite(HabitInviteStatus.PENDING);
        HabitTranslation translation = HabitTranslation.builder().name("Drink water").build();

        when(habitRepo.findById(1L)).thenReturn(Optional.of(getHabit()));
        when(userRepo.findById(2L)).thenReturn(Optional.of(buildUser(2L)));
        when(habitAssignRepo.findByHabitIdAndUserId(1L, 2L)).thenReturn(Optional.empty());
        when(habitInviteRepo.findByHabitIdAndInviterIdAndInviteeIdAndStatus(1L, inviterVO.getId(), 2L,
            HabitInviteStatus.PENDING)).thenReturn(Optional.of(existing));
        when(habitInviteRepo.save(existing)).thenReturn(existing);
        when(habitTranslationRepo.findByHabitAndLanguageCode(any(Habit.class), org.mockito.ArgumentMatchers.eq("en")))
            .thenReturn(Optional.of(translation));

        habitInviteService.sendInvite(1L, 2L, inviterVO);

        verify(habitInviteRepo).save(existing);
        verify(userRepo, never()).getReferenceById(any());
        verify(eventPublisher).publishEvent(any(HabitInviteNotificationEvent.class));
    }

    @Test
    void cancelInvite_UserIsInviter_CancelsAndPublishesNotificationEvent() {
        HabitInvite invite = getInvite(HabitInviteStatus.PENDING);
        UserVO inviterVO = getUserVO();
        inviterVO.setId(1L);

        when(habitInviteRepo.findById(1L)).thenReturn(Optional.of(invite));
        when(habitInviteRepo.save(invite)).thenReturn(invite);
        when(userRepo.findById(2L)).thenReturn(Optional.of(buildUser(2L)));
        when(modelMapper.map(any(User.class), org.mockito.ArgumentMatchers.eq(UserVO.class)))
            .thenReturn(getUserVO());
        when(habitTranslationRepo.findByHabitAndLanguageCode(any(Habit.class), org.mockito.ArgumentMatchers.eq("en")))
            .thenReturn(Optional.empty());

        HabitInviteDto dto = habitInviteService.cancelInvite(1L, inviterVO);

        assertEquals(HabitInviteStatus.CANCELLED, dto.getStatus());
        ArgumentCaptor<HabitInviteNotificationEvent> captor =
            ArgumentCaptor.forClass(HabitInviteNotificationEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(false, captor.getValue().isActive());
    }

    @Test
    void cancelInvite_UserIsNotInviter_ThrowsUserHasNoPermissionToAccessException() {
        HabitInvite invite = getInvite(HabitInviteStatus.PENDING);
        UserVO otherVO = getUserVO();
        otherVO.setId(99L);

        when(habitInviteRepo.findById(1L)).thenReturn(Optional.of(invite));

        assertThrows(UserHasNoPermissionToAccessException.class,
            () -> habitInviteService.cancelInvite(1L, otherVO));
        verify(habitInviteRepo, never()).save(any(HabitInvite.class));
    }

    @Test
    void cancelInvite_InviteNotFound_ThrowsNotFoundException() {
        when(habitInviteRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> habitInviteService.cancelInvite(1L, getUserVO()));
    }

    @Test
    void cancelInvite_InviteAlreadyHandled_ThrowsBadRequestException() {
        HabitInvite invite = getInvite(HabitInviteStatus.ACCEPTED);
        UserVO inviterVO = getUserVO();
        inviterVO.setId(1L);

        when(habitInviteRepo.findById(1L)).thenReturn(Optional.of(invite));

        assertThrows(BadRequestException.class, () -> habitInviteService.cancelInvite(1L, inviterVO));
        verify(habitInviteRepo, never()).save(any(HabitInvite.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void acceptInvite_UserIsInvitee_AcceptsAndPublishesNotificationEvent() {
        HabitInvite invite = getInvite(HabitInviteStatus.PENDING);
        UserVO inviteeVO = getUserVO();
        inviteeVO.setId(2L);

        when(habitInviteRepo.findById(1L)).thenReturn(Optional.of(invite));
        when(habitInviteRepo.save(invite)).thenReturn(invite);
        when(userRepo.findById(1L)).thenReturn(Optional.of(buildUser(1L)));
        when(modelMapper.map(any(User.class), org.mockito.ArgumentMatchers.eq(UserVO.class)))
            .thenReturn(getUserVO());
        when(habitTranslationRepo.findByHabitAndLanguageCode(any(Habit.class), org.mockito.ArgumentMatchers.eq("en")))
            .thenReturn(Optional.empty());

        HabitInviteDto dto = habitInviteService.acceptInvite(1L, inviteeVO);

        assertEquals(HabitInviteStatus.ACCEPTED, dto.getStatus());
        verify(eventPublisher).publishEvent(any(HabitInviteNotificationEvent.class));
    }

    @Test
    void acceptInvite_UserIsNotInvitee_ThrowsUserHasNoPermissionToAccessException() {
        HabitInvite invite = getInvite(HabitInviteStatus.PENDING);
        UserVO otherVO = getUserVO();
        otherVO.setId(99L);

        when(habitInviteRepo.findById(1L)).thenReturn(Optional.of(invite));

        assertThrows(UserHasNoPermissionToAccessException.class,
            () -> habitInviteService.acceptInvite(1L, otherVO));
        verify(habitInviteRepo, never()).save(any(HabitInvite.class));
    }

    @Test
    void declineInvite_UserIsInvitee_DeclinesAndPublishesNotificationEvent() {
        HabitInvite invite = getInvite(HabitInviteStatus.PENDING);
        UserVO inviteeVO = getUserVO();
        inviteeVO.setId(2L);

        when(habitInviteRepo.findById(1L)).thenReturn(Optional.of(invite));
        when(habitInviteRepo.save(invite)).thenReturn(invite);
        when(userRepo.findById(1L)).thenReturn(Optional.of(buildUser(1L)));
        when(modelMapper.map(any(User.class), org.mockito.ArgumentMatchers.eq(UserVO.class)))
            .thenReturn(getUserVO());
        when(habitTranslationRepo.findByHabitAndLanguageCode(any(Habit.class), org.mockito.ArgumentMatchers.eq("en")))
            .thenReturn(Optional.empty());

        HabitInviteDto dto = habitInviteService.declineInvite(1L, inviteeVO);

        assertEquals(HabitInviteStatus.DECLINED, dto.getStatus());
        verify(eventPublisher).publishEvent(any(HabitInviteNotificationEvent.class));
    }

    @Test
    void getSentPendingInvites_ReturnsPageOfInviterOwnPendingInvites() {
        Pageable pageable = PageRequest.of(0, 10);
        HabitInvite invite = getInvite(HabitInviteStatus.PENDING);
        UserVO inviterVO = getUserVO();
        inviterVO.setId(1L);

        when(habitInviteRepo.findAllByInviterIdAndStatusOrderByCreatedDateDesc(1L, HabitInviteStatus.PENDING,
            pageable)).thenReturn(new PageImpl<>(List.of(invite), pageable, 1));
        when(habitTranslationRepo.findByHabitAndLanguageCode(any(Habit.class), org.mockito.ArgumentMatchers.eq("en")))
            .thenReturn(Optional.empty());

        PageableDto<HabitInviteDto> result = habitInviteService.getSentPendingInvites(inviterVO, pageable);

        assertEquals(1, result.getPage().size());
        assertEquals(1L, result.getPage().getFirst().getId());
    }
}
