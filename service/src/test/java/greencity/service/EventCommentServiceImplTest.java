package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.eventcomment.AddEventCommentDtoRequest;
import greencity.dto.eventcomment.AddEventCommentDtoResponse;
import greencity.dto.eventcomment.EventCommentDto;
import greencity.dto.user.UserVO;
import greencity.entity.User;
import greencity.entity.event.Event;
import greencity.entity.event.EventComment;
import greencity.enums.Role;
import greencity.event.EventCommentNotificationEvent;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.repository.EventCommentRepo;
import greencity.repository.EventRepo;
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
class EventCommentServiceImplTest {
    @Mock
    private EventCommentRepo eventCommentRepo;
    @Mock
    private EventRepo eventRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private EventCommentServiceImpl eventCommentService;

    private Event getEvent(User organizer) {
        Event event = new Event();
        event.setId(1L);
        event.setTitle("Cleanup day");
        event.setOrganizer(organizer);
        return event;
    }

    private User buildUser(long id) {
        User user = getUser();
        user.setId(id);
        return user;
    }

    @Test
    void save_CommenterIsNotOrganizer_SavesCommentAndPublishesNotificationEvent() {
        User organizer = buildUser(2L);
        User commenter = buildUser(1L);
        Event event = getEvent(organizer);
        UserVO commenterVO = getUserVO();
        UserVO organizerVO = getUserVO();
        organizerVO.setId(2L);
        AddEventCommentDtoRequest request = AddEventCommentDtoRequest.builder().text("Nice event!").build();

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(userRepo.findById(1L)).thenReturn(Optional.of(commenter));
        when(eventCommentRepo.save(any(EventComment.class))).then(AdditionalAnswers.returnsFirstArg());
        when(modelMapper.map(organizer, UserVO.class)).thenReturn(organizerVO);

        AddEventCommentDtoResponse response = eventCommentService.save(1L, request, commenterVO);

        assertEquals("Nice event!", response.getText());
        assertEquals(1L, response.getAuthor().getId());

        ArgumentCaptor<EventCommentNotificationEvent> captor =
            ArgumentCaptor.forClass(EventCommentNotificationEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        EventCommentNotificationEvent published = captor.getValue();
        assertEquals(organizerVO, published.getOrganizer());
        assertEquals(commenterVO, published.getCommenter());
        assertEquals(1L, published.getEventId());
        assertEquals("Cleanup day", published.getEventTitle());
    }

    @Test
    void save_CommenterIsOrganizer_SavesCommentWithoutPublishingNotificationEvent() {
        User organizer = buildUser(1L);
        Event event = getEvent(organizer);
        UserVO commenterVO = getUserVO();
        AddEventCommentDtoRequest request = AddEventCommentDtoRequest.builder().text("My own event").build();

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(userRepo.findById(1L)).thenReturn(Optional.of(organizer));
        when(eventCommentRepo.save(any(EventComment.class))).then(AdditionalAnswers.returnsFirstArg());

        eventCommentService.save(1L, request, commenterVO);

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void save_EventNotFound_ThrowsNotFoundException() {
        AddEventCommentDtoRequest request = AddEventCommentDtoRequest.builder().text("text").build();
        when(eventRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventCommentService.save(1L, request, getUserVO()));
        verify(eventCommentRepo, never()).save(any(EventComment.class));
    }

    @Test
    void findAllComments_EventExists_ReturnsPageOfComments() {
        Pageable pageable = PageRequest.of(0, 10);
        EventComment comment = EventComment.builder()
            .id(1L)
            .text("text")
            .createdDate(LocalDateTime.now())
            .user(getUser())
            .build();
        EventCommentDto dto = EventCommentDto.builder().id(1L).text("text").build();

        when(eventRepo.existsById(1L)).thenReturn(true);
        when(eventCommentRepo.findAllByEventIdOrderByCreatedDateDesc(eq(1L), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(comment), pageable, 1));
        when(modelMapper.map(comment, EventCommentDto.class)).thenReturn(dto);

        PageableDto<EventCommentDto> result = eventCommentService.findAllComments(pageable, 1L);

        assertEquals(1, result.getPage().size());
        assertEquals(dto, result.getPage().getFirst());
    }

    @Test
    void findAllComments_EventMissing_ThrowsNotFoundException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(eventRepo.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> eventCommentService.findAllComments(pageable, 1L));
    }

    @Test
    void deleteById_UserIsAuthor_DeletesComment() {
        User author = buildUser(1L);
        EventComment comment = EventComment.builder().id(1L).user(author).build();
        UserVO userVO = getUserVO();

        when(eventCommentRepo.findById(1L)).thenReturn(Optional.of(comment));

        eventCommentService.deleteById(1L, userVO);

        verify(eventCommentRepo).delete(comment);
    }

    @Test
    void deleteById_UserIsAdmin_DeletesComment() {
        User author = buildUser(2L);
        EventComment comment = EventComment.builder().id(1L).user(author).build();
        UserVO adminVO = getUserVO();
        adminVO.setRole(Role.ROLE_ADMIN);

        when(eventCommentRepo.findById(1L)).thenReturn(Optional.of(comment));

        eventCommentService.deleteById(1L, adminVO);

        verify(eventCommentRepo).delete(comment);
    }

    @Test
    void deleteById_UserIsNeitherAuthorNorAdmin_ThrowsUserHasNoPermissionToAccessException() {
        User author = buildUser(2L);
        EventComment comment = EventComment.builder().id(1L).user(author).build();
        UserVO userVO = getUserVO();

        when(eventCommentRepo.findById(1L)).thenReturn(Optional.of(comment));

        assertThrows(UserHasNoPermissionToAccessException.class,
            () -> eventCommentService.deleteById(1L, userVO));
        verify(eventCommentRepo, never()).delete(any(EventComment.class));
    }

    @Test
    void deleteById_CommentMissing_ThrowsNotFoundException() {
        when(eventCommentRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventCommentService.deleteById(1L, getUserVO()));
    }
}
