package greencity.service;

import greencity.dto.eventlike.EventLikeDtoResponse;
import greencity.dto.user.UserVO;
import greencity.entity.User;
import greencity.entity.event.Event;
import greencity.entity.event.EventLike;
import greencity.event.EventLikeNotificationEvent;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.EventLikeRepo;
import greencity.repository.EventRepo;
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
class EventLikeServiceImplTest {
    @Mock
    private EventLikeRepo eventLikeRepo;
    @Mock
    private EventRepo eventRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private EventLikeServiceImpl eventLikeService;

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
    void like_LikerIsNotOrganizer_SavesLikeAndPublishesNotificationEvent() {
        User organizer = buildUser(2L);
        User liker = buildUser(1L);
        Event event = getEvent(organizer);
        UserVO likerVO = getUserVO();
        UserVO organizerVO = getUserVO();
        organizerVO.setId(2L);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(eventLikeRepo.existsByEventIdAndUserId(1L, 1L)).thenReturn(false, true);
        when(userRepo.findById(1L)).thenReturn(Optional.of(liker));
        when(userRepo.findById(2L)).thenReturn(Optional.of(organizer));
        when(modelMapper.map(organizer, UserVO.class)).thenReturn(organizerVO);
        when(eventLikeRepo.countByEventId(1L)).thenReturn(1L);

        EventLikeDtoResponse response = eventLikeService.like(1L, likerVO);

        assertEquals(1L, response.getEventId());
        assertEquals(1L, response.getLikesCount());
        assertTrue(response.getLiked());
        verify(eventLikeRepo).save(any(EventLike.class));

        ArgumentCaptor<EventLikeNotificationEvent> captor =
            ArgumentCaptor.forClass(EventLikeNotificationEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        EventLikeNotificationEvent published = captor.getValue();
        assertEquals(organizerVO, published.getOrganizer());
        assertEquals(likerVO, published.getLiker());
        assertEquals(1L, published.getEventId());
        assertEquals("Cleanup day", published.getEventTitle());
        assertTrue(published.isLiked());
    }

    @Test
    void like_LikerIsOrganizer_SavesLikeWithoutPublishingNotificationEvent() {
        User organizer = buildUser(1L);
        Event event = getEvent(organizer);
        UserVO organizerVO = getUserVO();

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(eventLikeRepo.existsByEventIdAndUserId(1L, 1L)).thenReturn(false, true);
        when(userRepo.findById(1L)).thenReturn(Optional.of(organizer));
        when(eventLikeRepo.countByEventId(1L)).thenReturn(1L);

        eventLikeService.like(1L, organizerVO);

        verify(eventLikeRepo).save(any(EventLike.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void like_AlreadyLiked_DoesNotSaveDuplicateOrPublish() {
        User organizer = buildUser(2L);
        Event event = getEvent(organizer);
        UserVO likerVO = getUserVO();

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(eventLikeRepo.existsByEventIdAndUserId(1L, 1L)).thenReturn(true);
        when(eventLikeRepo.countByEventId(1L)).thenReturn(1L);

        eventLikeService.like(1L, likerVO);

        verify(eventLikeRepo, never()).save(any(EventLike.class));
        verifyNoInteractions(eventPublisher);
        verifyNoInteractions(userRepo);
    }

    @Test
    void like_EventNotFound_ThrowsNotFoundException() {
        when(eventRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventLikeService.like(1L, getUserVO()));
        verify(eventLikeRepo, never()).save(any(EventLike.class));
    }

    @Test
    void like_LikerIsNotOrganizer_LoadsOrganizerInsteadOfMappingLazyProxy() {
        User organizer = buildUser(2L);
        User liker = buildUser(1L);
        Event event = getEvent(organizer);
        UserVO likerVO = getUserVO();
        UserVO organizerVO = getUserVO();
        organizerVO.setId(2L);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(eventLikeRepo.existsByEventIdAndUserId(1L, 1L)).thenReturn(false, true);
        when(userRepo.findById(1L)).thenReturn(Optional.of(liker));
        when(userRepo.findById(2L)).thenReturn(Optional.of(organizer));
        when(modelMapper.map(organizer, UserVO.class)).thenReturn(organizerVO);
        when(eventLikeRepo.countByEventId(1L)).thenReturn(1L);

        eventLikeService.like(1L, likerVO);

        // Event.organizer is a lazy proxy that ModelMapper would map to an
        // all-null UserVO, so the organizer must be re-read through the repo.
        verify(userRepo).findById(2L);
    }

    @Test
    void unlike_LikeExists_DeletesLikeAndPublishesNotificationEvent() {
        User organizer = buildUser(2L);
        Event event = getEvent(organizer);
        UserVO likerVO = getUserVO();
        UserVO organizerVO = getUserVO();
        organizerVO.setId(2L);
        EventLike like = EventLike.builder().id(10L).event(event).user(buildUser(1L)).build();

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(eventLikeRepo.findByEventIdAndUserId(1L, 1L)).thenReturn(Optional.of(like));
        when(userRepo.findById(2L)).thenReturn(Optional.of(organizer));
        when(modelMapper.map(organizer, UserVO.class)).thenReturn(organizerVO);
        when(eventLikeRepo.existsByEventIdAndUserId(1L, 1L)).thenReturn(false);
        when(eventLikeRepo.countByEventId(1L)).thenReturn(0L);

        EventLikeDtoResponse response = eventLikeService.unlike(1L, likerVO);

        assertFalse(response.getLiked());
        verify(eventLikeRepo).delete(like);

        ArgumentCaptor<EventLikeNotificationEvent> captor =
            ArgumentCaptor.forClass(EventLikeNotificationEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertFalse(captor.getValue().isLiked());
    }

    @Test
    void unlike_LikeDoesNotExist_DoesNothing() {
        Event event = getEvent(buildUser(2L));
        UserVO likerVO = getUserVO();

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(eventLikeRepo.findByEventIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(eventLikeRepo.existsByEventIdAndUserId(1L, 1L)).thenReturn(false);
        when(eventLikeRepo.countByEventId(1L)).thenReturn(0L);

        eventLikeService.unlike(1L, likerVO);

        verify(eventLikeRepo, never()).delete(any(EventLike.class));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void unlike_EventNotFound_ThrowsNotFoundException() {
        when(eventRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventLikeService.unlike(1L, getUserVO()));
        verify(eventLikeRepo, never()).delete(any(EventLike.class));
    }
}
