package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.PageableDto;
import greencity.dto.eventcomment.AddEventCommentDtoRequest;
import greencity.dto.eventcomment.AddEventCommentDtoResponse;
import greencity.dto.eventcomment.EventCommentAuthorDto;
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
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventCommentServiceImpl implements EventCommentService {
    private final EventCommentRepo eventCommentRepo;
    private final EventRepo eventRepo;
    private final UserRepo userRepo;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public AddEventCommentDtoResponse save(Long eventId, AddEventCommentDtoRequest addEventCommentDtoRequest,
        UserVO userVO) {
        Event event = eventRepo.findById(eventId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.EVENT_NOT_FOUND_BY_ID + eventId));
        User user = userRepo.findById(userVO.getId())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + userVO.getId()));

        EventComment comment = EventComment.builder()
            .text(addEventCommentDtoRequest.getText())
            .user(user)
            .event(event)
            .build();
        EventComment savedComment = eventCommentRepo.save(comment);

        User organizer = event.getOrganizer();
        if (organizer != null && !userVO.getId().equals(organizer.getId())) {
            eventPublisher.publishEvent(EventCommentNotificationEvent.builder()
                .organizer(modelMapper.map(organizer, UserVO.class))
                .commenter(userVO)
                .eventId(eventId)
                .eventTitle(event.getTitle())
                .build());
        }

        return AddEventCommentDtoResponse.builder()
            .id(savedComment.getId())
            .text(savedComment.getText())
            .createdDate(savedComment.getCreatedDate())
            .author(EventCommentAuthorDto.builder()
                .id(user.getId())
                .name(user.getName())
                .userProfilePicturePath(user.getProfilePicturePath())
                .build())
            .build();
    }

    @Override
    public PageableDto<EventCommentDto> findAllComments(Pageable pageable, Long eventId) {
        if (!eventRepo.existsById(eventId)) {
            throw new NotFoundException(ErrorMessage.EVENT_NOT_FOUND_BY_ID + eventId);
        }
        Page<EventComment> comments = eventCommentRepo.findAllByEventIdOrderByCreatedDateDesc(eventId, pageable);
        List<EventCommentDto> dtoList = comments.stream()
            .map(comment -> modelMapper.map(comment, EventCommentDto.class))
            .toList();
        return new PageableDto<>(
            dtoList,
            comments.getTotalElements(),
            comments.getPageable().getPageNumber(),
            comments.getTotalPages());
    }

    @Override
    @Transactional
    public void deleteById(Long id, UserVO userVO) {
        EventComment comment = eventCommentRepo.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.COMMENT_NOT_FOUND_EXCEPTION));

        if (userVO.getRole() != Role.ROLE_ADMIN && !userVO.getId().equals(comment.getUser().getId())) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }
        eventCommentRepo.delete(comment);
    }
}
