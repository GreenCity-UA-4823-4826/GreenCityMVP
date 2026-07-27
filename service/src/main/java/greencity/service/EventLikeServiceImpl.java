package greencity.service;

import greencity.constant.ErrorMessage;
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
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventLikeServiceImpl implements EventLikeService {
    private final EventLikeRepo eventLikeRepo;
    private final EventRepo eventRepo;
    private final UserRepo userRepo;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public EventLikeDtoResponse like(Long eventId, UserVO userVO) {
        Event event = findEvent(eventId);
        if (!eventLikeRepo.existsByEventIdAndUserId(eventId, userVO.getId())) {
            User user = userRepo.findById(userVO.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + userVO.getId()));
            eventLikeRepo.save(EventLike.builder()
                .event(event)
                .user(user)
                .build());
            publishNotification(event, eventId, userVO, true);
        }
        return buildResponse(eventId, userVO.getId());
    }

    @Override
    @Transactional
    public EventLikeDtoResponse unlike(Long eventId, UserVO userVO) {
        Event event = findEvent(eventId);
        eventLikeRepo.findByEventIdAndUserId(eventId, userVO.getId()).ifPresent(like -> {
            eventLikeRepo.delete(like);
            publishNotification(event, eventId, userVO, false);
        });
        return buildResponse(eventId, userVO.getId());
    }

    private Event findEvent(Long eventId) {
        return eventRepo.findById(eventId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.EVENT_NOT_FOUND_BY_ID + eventId));
    }

    // event.getOrganizer() is a lazy proxy; reading its id does not initialize
    // it, but ModelMapper is configured for private field access and would map
    // a proxy to an all-null UserVO, so load the organizer before mapping.
    private void publishNotification(Event event, Long eventId, UserVO userVO, boolean liked) {
        Long organizerId = event.getOrganizer() == null ? null : event.getOrganizer().getId();
        if (organizerId == null || userVO.getId().equals(organizerId)) {
            return;
        }
        User organizer = userRepo.findById(organizerId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + organizerId));
        eventPublisher.publishEvent(EventLikeNotificationEvent.builder()
            .organizer(modelMapper.map(organizer, UserVO.class))
            .liker(userVO)
            .eventId(eventId)
            .eventTitle(event.getTitle())
            .liked(liked)
            .build());
    }

    private EventLikeDtoResponse buildResponse(Long eventId, Long userId) {
        return EventLikeDtoResponse.builder()
            .eventId(eventId)
            .likesCount(eventLikeRepo.countByEventId(eventId))
            .liked(eventLikeRepo.existsByEventIdAndUserId(eventId, userId))
            .build();
    }
}
