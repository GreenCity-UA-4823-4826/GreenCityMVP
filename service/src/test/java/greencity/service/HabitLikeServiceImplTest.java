package greencity.service;

import greencity.dto.habitlike.HabitLikeDtoResponse;
import greencity.dto.user.UserVO;
import greencity.entity.Habit;
import greencity.entity.HabitLike;
import greencity.entity.HabitTranslation;
import greencity.entity.User;
import greencity.event.HabitLikeNotificationEvent;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.HabitLikeRepo;
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
import java.util.Optional;

import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.getUserVO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabitLikeServiceImplTest {
    @Mock
    private HabitLikeRepo habitLikeRepo;
    @Mock
    private HabitRepo habitRepo;
    @Mock
    private HabitTranslationRepo habitTranslationRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private HabitLikeServiceImpl habitLikeService;

    private Habit getHabit(Long ownerId) {
        return Habit.builder()
            .id(1L)
            .userId(ownerId)
            .isCustomHabit(ownerId != null)
            .build();
    }

    private User buildUser(long id) {
        User user = getUser();
        user.setId(id);
        return user;
    }

    @Test
    void like_LikerIsNotOwner_SavesLikeAndPublishesNotificationEvent() {
        Habit habit = getHabit(2L);
        User owner = buildUser(2L);
        User liker = buildUser(1L);
        UserVO likerVO = getUserVO();
        UserVO ownerVO = getUserVO();
        ownerVO.setId(2L);
        HabitTranslation translation = HabitTranslation.builder().name("Drink water").build();

        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(habitLikeRepo.existsByHabitIdAndUserId(1L, 1L)).thenReturn(false, true);
        when(userRepo.findById(1L)).thenReturn(Optional.of(liker));
        when(userRepo.findById(2L)).thenReturn(Optional.of(owner));
        when(modelMapper.map(owner, UserVO.class)).thenReturn(ownerVO);
        when(habitLikeRepo.countByHabitId(1L)).thenReturn(1L);
        when(habitTranslationRepo.findByHabitAndLanguageCode(habit, "en")).thenReturn(Optional.of(translation));

        HabitLikeDtoResponse response = habitLikeService.like(1L, likerVO);

        assertEquals(1L, response.getHabitId());
        assertEquals(1L, response.getLikesCount());
        assertTrue(response.getLiked());
        verify(habitLikeRepo).save(any(HabitLike.class));

        ArgumentCaptor<HabitLikeNotificationEvent> captor =
            ArgumentCaptor.forClass(HabitLikeNotificationEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        HabitLikeNotificationEvent published = captor.getValue();
        assertEquals(ownerVO, published.getOwner());
        assertEquals(likerVO, published.getLiker());
        assertEquals(1L, published.getHabitId());
        assertEquals("Drink water", published.getHabitName());
        assertTrue(published.isLiked());
    }

    @Test
    void like_LikerIsOwner_SavesLikeWithoutPublishingNotificationEvent() {
        Habit habit = getHabit(1L);
        User owner = buildUser(1L);
        UserVO ownerVO = getUserVO();

        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(habitLikeRepo.existsByHabitIdAndUserId(1L, 1L)).thenReturn(false, true);
        when(userRepo.findById(1L)).thenReturn(Optional.of(owner));
        when(habitLikeRepo.countByHabitId(1L)).thenReturn(1L);

        habitLikeService.like(1L, ownerVO);

        verify(habitLikeRepo).save(any(HabitLike.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void like_HabitHasNoOwner_SavesLikeWithoutPublishingNotificationEvent() {
        Habit habit = getHabit(null);
        User liker = buildUser(1L);
        UserVO likerVO = getUserVO();

        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(habitLikeRepo.existsByHabitIdAndUserId(1L, 1L)).thenReturn(false, true);
        when(userRepo.findById(1L)).thenReturn(Optional.of(liker));
        when(habitLikeRepo.countByHabitId(1L)).thenReturn(1L);

        habitLikeService.like(1L, likerVO);

        verify(habitLikeRepo).save(any(HabitLike.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void like_AlreadyLiked_DoesNotSaveDuplicateOrPublish() {
        Habit habit = getHabit(2L);
        UserVO likerVO = getUserVO();

        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(habitLikeRepo.existsByHabitIdAndUserId(1L, 1L)).thenReturn(true);
        when(habitLikeRepo.countByHabitId(1L)).thenReturn(1L);

        habitLikeService.like(1L, likerVO);

        verify(habitLikeRepo, never()).save(any(HabitLike.class));
        verifyNoInteractions(eventPublisher);
        verifyNoInteractions(userRepo);
    }

    @Test
    void like_HabitNotFound_ThrowsNotFoundException() {
        when(habitRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> habitLikeService.like(1L, getUserVO()));
        verify(habitLikeRepo, never()).save(any(HabitLike.class));
    }

    @Test
    void unlike_LikeExists_DeletesLikeAndPublishesNotificationEvent() {
        Habit habit = getHabit(2L);
        User owner = buildUser(2L);
        UserVO likerVO = getUserVO();
        UserVO ownerVO = getUserVO();
        ownerVO.setId(2L);
        HabitLike like = HabitLike.builder().id(10L).habit(habit).user(buildUser(1L)).build();

        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(habitLikeRepo.findByHabitIdAndUserId(1L, 1L)).thenReturn(Optional.of(like));
        when(userRepo.findById(2L)).thenReturn(Optional.of(owner));
        when(modelMapper.map(owner, UserVO.class)).thenReturn(ownerVO);
        when(habitLikeRepo.existsByHabitIdAndUserId(1L, 1L)).thenReturn(false);
        when(habitLikeRepo.countByHabitId(1L)).thenReturn(0L);

        HabitLikeDtoResponse response = habitLikeService.unlike(1L, likerVO);

        assertFalse(response.getLiked());
        verify(habitLikeRepo).delete(like);

        ArgumentCaptor<HabitLikeNotificationEvent> captor =
            ArgumentCaptor.forClass(HabitLikeNotificationEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertFalse(captor.getValue().isLiked());
    }

    @Test
    void unlike_LikeDoesNotExist_DoesNothing() {
        Habit habit = getHabit(2L);
        UserVO likerVO = getUserVO();

        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(habitLikeRepo.findByHabitIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(habitLikeRepo.existsByHabitIdAndUserId(1L, 1L)).thenReturn(false);
        when(habitLikeRepo.countByHabitId(1L)).thenReturn(0L);

        habitLikeService.unlike(1L, likerVO);

        verify(habitLikeRepo, never()).delete(any(HabitLike.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void unlike_HabitNotFound_ThrowsNotFoundException() {
        when(habitRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> habitLikeService.unlike(1L, getUserVO()));
        verify(habitLikeRepo, never()).delete(any(HabitLike.class));
    }
}
