package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.habitcomment.AddHabitCommentDtoRequest;
import greencity.dto.habitcomment.AddHabitCommentDtoResponse;
import greencity.dto.habitcomment.HabitCommentDto;
import greencity.dto.user.UserVO;
import greencity.entity.Habit;
import greencity.entity.HabitComment;
import greencity.entity.HabitTranslation;
import greencity.entity.User;
import greencity.enums.Role;
import greencity.event.HabitCommentNotificationEvent;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.repository.HabitCommentRepo;
import greencity.repository.HabitRepo;
import greencity.repository.HabitTranslationRepo;
import greencity.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabitCommentServiceImplTest {
    @Mock
    private HabitCommentRepo habitCommentRepo;
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
    private HabitCommentServiceImpl habitCommentService;

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
    void save_CommenterIsNotOwner_SavesCommentAndPublishesNotificationEvent() {
        Habit habit = getHabit(2L);
        User owner = buildUser(2L);
        User commenter = buildUser(1L);
        UserVO commenterVO = getUserVO();
        UserVO ownerVO = getUserVO();
        ownerVO.setId(2L);
        AddHabitCommentDtoRequest request = AddHabitCommentDtoRequest.builder().text("Great habit!").build();
        HabitTranslation translation = HabitTranslation.builder().name("Drink water").build();

        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(userRepo.findById(1L)).thenReturn(Optional.of(commenter));
        when(userRepo.findById(2L)).thenReturn(Optional.of(owner));
        when(habitCommentRepo.save(any(HabitComment.class))).then(AdditionalAnswers.returnsFirstArg());
        when(modelMapper.map(owner, UserVO.class)).thenReturn(ownerVO);
        when(habitTranslationRepo.findByHabitAndLanguageCode(habit, "en")).thenReturn(Optional.of(translation));

        AddHabitCommentDtoResponse response = habitCommentService.save(1L, request, commenterVO);

        assertEquals("Great habit!", response.getText());
        assertEquals(1L, response.getAuthor().getId());

        ArgumentCaptor<HabitCommentNotificationEvent> captor =
            ArgumentCaptor.forClass(HabitCommentNotificationEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        HabitCommentNotificationEvent published = captor.getValue();
        assertEquals(ownerVO, published.getOwner());
        assertEquals(commenterVO, published.getCommenter());
        assertEquals(1L, published.getHabitId());
        assertEquals("Drink water", published.getHabitName());
    }

    @Test
    void save_CommenterIsOwner_SavesCommentWithoutPublishingNotificationEvent() {
        Habit habit = getHabit(1L);
        User owner = buildUser(1L);
        UserVO commenterVO = getUserVO();
        AddHabitCommentDtoRequest request = AddHabitCommentDtoRequest.builder().text("My own habit").build();

        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(userRepo.findById(1L)).thenReturn(Optional.of(owner));
        when(habitCommentRepo.save(any(HabitComment.class))).then(AdditionalAnswers.returnsFirstArg());

        habitCommentService.save(1L, request, commenterVO);

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void save_HabitHasNoOwner_SavesCommentWithoutPublishingNotificationEvent() {
        Habit habit = getHabit(null);
        User commenter = buildUser(1L);
        UserVO commenterVO = getUserVO();
        AddHabitCommentDtoRequest request = AddHabitCommentDtoRequest.builder().text("Default habit").build();

        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(userRepo.findById(1L)).thenReturn(Optional.of(commenter));
        when(habitCommentRepo.save(any(HabitComment.class))).then(AdditionalAnswers.returnsFirstArg());

        habitCommentService.save(1L, request, commenterVO);

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void save_HabitNotFound_ThrowsNotFoundException() {
        AddHabitCommentDtoRequest request = AddHabitCommentDtoRequest.builder().text("text").build();
        when(habitRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> habitCommentService.save(1L, request, getUserVO()));
        verify(habitCommentRepo, never()).save(any(HabitComment.class));
    }

    @Test
    void findAllComments_HabitExists_ReturnsPageOfComments() {
        Pageable pageable = PageRequest.of(0, 10);
        HabitComment comment = HabitComment.builder()
            .id(1L)
            .text("text")
            .createdDate(LocalDateTime.now())
            .user(getUser())
            .build();
        HabitCommentDto dto = HabitCommentDto.builder().id(1L).text("text").build();

        when(habitRepo.existsById(1L)).thenReturn(true);
        when(habitCommentRepo.findAllByHabitIdOrderByCreatedDateDesc(eq(1L), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(comment), pageable, 1));
        when(modelMapper.map(comment, HabitCommentDto.class)).thenReturn(dto);

        PageableDto<HabitCommentDto> result = habitCommentService.findAllComments(pageable, 1L);

        assertEquals(1, result.getPage().size());
        assertEquals(dto, result.getPage().getFirst());
    }

    @Test
    void findAllComments_HabitMissing_ThrowsNotFoundException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(habitRepo.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> habitCommentService.findAllComments(pageable, 1L));
    }

    @Test
    void deleteById_UserIsAuthor_DeletesComment() {
        User author = buildUser(1L);
        HabitComment comment = HabitComment.builder().id(1L).user(author).build();
        UserVO userVO = getUserVO();

        when(habitCommentRepo.findById(1L)).thenReturn(Optional.of(comment));

        habitCommentService.deleteById(1L, userVO);

        verify(habitCommentRepo).delete(comment);
    }

    @Test
    void deleteById_UserIsAdmin_DeletesComment() {
        User author = buildUser(2L);
        HabitComment comment = HabitComment.builder().id(1L).user(author).build();
        UserVO adminVO = getUserVO();
        adminVO.setRole(Role.ROLE_ADMIN);

        when(habitCommentRepo.findById(1L)).thenReturn(Optional.of(comment));

        habitCommentService.deleteById(1L, adminVO);

        verify(habitCommentRepo).delete(comment);
    }

    @Test
    void deleteById_UserIsNeitherAuthorNorAdmin_ThrowsUserHasNoPermissionToAccessException() {
        User author = buildUser(2L);
        HabitComment comment = HabitComment.builder().id(1L).user(author).build();
        UserVO userVO = getUserVO();

        when(habitCommentRepo.findById(1L)).thenReturn(Optional.of(comment));

        assertThrows(UserHasNoPermissionToAccessException.class,
            () -> habitCommentService.deleteById(1L, userVO));
        verify(habitCommentRepo, never()).delete(any(HabitComment.class));
    }

    @Test
    void deleteById_CommentMissing_ThrowsNotFoundException() {
        when(habitCommentRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> habitCommentService.deleteById(1L, getUserVO()));
    }
}
