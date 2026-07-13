package greencity.service;

import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableDto;
import greencity.dto.event.*;
import greencity.dto.user.UserVO;
import greencity.entity.User;
import greencity.entity.event.Event;
import greencity.entity.event.EventAttendance;
import greencity.entity.event.EventImage;
import greencity.enums.Role;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.mapping.event.*;
import greencity.repository.EventAttendanceRepo;
import greencity.repository.EventRepo;
import greencity.repository.UserRepo;
import greencity.validator.ImageSizeValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final int MAX_IMAGES = 5;
    private static final List<String> VALID_IMAGE_TYPES =
            List.of("image/jpeg", "image/png", "image/jpg");

    private final EventRepo eventRepo;
    private final UserRepo userRepo;
    private final FileService fileService;
    private final EventCreateRequestDtoMapper eventCreateRequestDtoMapper;
    private final EventResponseDtoMapper eventResponseDtoMapper;
    private final EventAttendanceRepo attendanceRepo;
    private final MyEventResponseDtoMapper myEventResponseDtoMapper;
    private final EventPreviewResponseDtoMapper eventPreviewResponseDtoMapper;

    @Override
    @Transactional
    public EventResponseDto createEvent(EventCreateRequestDto eventCreateRequestDto,
                                        MultipartFile[] images,
                                        Integer mainImageIndex,
                                        UserVO userVO) {

        User organizer = userRepo.findById(userVO.getId())
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.USER_NOT_FOUND_BY_ID + userVO.getId()));

        Event event = eventCreateRequestDtoMapper.toEntity(eventCreateRequestDto, organizer);

        List<EventImage> eventImages = buildEventImages(images, mainImageIndex);

        eventImages.forEach(image -> image.setEvent(event));
        event.setImages(eventImages);

        Event savedEvent = eventRepo.save(event);

        return eventResponseDtoMapper.convert(savedEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId, UserVO userVO) {
        Event event = eventRepo.findById(eventId)
            .orElseThrow(() -> new NotFoundException(
                    ErrorMessage.EVENT_NOT_FOUND_BY_ID + eventId));

        if (userVO.getRole() != Role.ROLE_ADMIN
                && !userVO.getId().equals(event.getOrganizer().getId())) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }

        for (EventImage image : event.getImages()) {
            if (!image.getImageUrl().equals(AppConstant.DEFAULT_EVENT_IMAGE)) {
                fileService.delete(image.getImageUrl());
            }
        }

        eventRepo.delete(event);
    }

    @Override
    @Transactional
    public PageableDto<MyEventResponseDto> getMyEvents(UserVO userVO, Pageable pageable) {
        User user = userRepo.findById(userVO.getId())
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.USER_NOT_FOUND_BY_ID + userVO.getId()));

        List<Event> organizedEvents = eventRepo.findByOrganizerId(user.getId());
        List<EventAttendance> joinedEvents = attendanceRepo.findByUserId(user.getId());

        List<MyEventResponseDto> result = new ArrayList<>();

        organizedEvents.stream()
                .map(myEventResponseDtoMapper::fromEvent)
                .forEach(result::add);

        joinedEvents.stream()
                .filter(attendance -> !attendance.getEvent().getOrganizer().getId().equals(user.getId()))
                .map(myEventResponseDtoMapper::fromAttendance)
                .forEach(result::add);

        int start = Math.min((int) pageable.getOffset(), result.size());
        int end = Math.min(start + pageable.getPageSize(), result.size());

        List<MyEventResponseDto> pageContent = result.subList(start, end);

        return new PageableDto<>(
                pageContent,
                result.size(),
                pageable.getPageNumber(),
                (int) Math.ceil((double) result.size() / pageable.getPageSize())
        );
    }

    @Override
    public List<EventSearchSuggestionResponseDto> getSearchSuggestions(String query) {
        if (query.length() > 64) {
            throw new BadRequestException(ErrorMessage.SEARCH_QUERY_TOO_LONG);
        }
        return eventRepo.findTitleSuggestions(query);
    }

    @Override
    public List<EventPreviewResponseDto> searchEvents(String query) {
        if (query.length() > 64) {
            throw new BadRequestException(ErrorMessage.SEARCH_QUERY_TOO_LONG);
        }
        List<Event> events = eventRepo.searchByTitle(query);
        return events.stream()
                .map(eventPreviewResponseDtoMapper::toDto)
                .toList();
    }

    private List<EventImage> buildEventImages(MultipartFile[] images, Integer mainImageIndex) {
        List<EventImage> eventImages = new ArrayList<>();

        if (images == null || images.length == 0) {
            EventImage defaultImage = new EventImage();
            defaultImage.setImageUrl(AppConstant.DEFAULT_EVENT_IMAGE);
            defaultImage.setMainImage(true);
            eventImages.add(defaultImage);
            return eventImages;
        }

        if (images.length > MAX_IMAGES) {
            throw new BadRequestException(ErrorMessage.TOO_MANY_EVENT_IMAGES);
        }

        if (mainImageIndex != null && (mainImageIndex < 0 || mainImageIndex >= images.length)) {
            throw new BadRequestException(ErrorMessage.INVALID_MAIN_IMAGE_INDEX);
        }

        for (int i = 0; i < images.length; i++) {
            MultipartFile image = images[i];
            validateImage(image);

            String uploadedUrl = fileService.upload(image);

            EventImage eventImage = new EventImage();
            eventImage.setImageUrl(uploadedUrl);
            boolean isMain = mainImageIndex != null ? mainImageIndex == i : i == 0;
            eventImage.setMainImage(isMain);

            eventImages.add(eventImage);
        }

        return eventImages;
    }

    private void validateImage(MultipartFile image) {
        if (image.getContentType() == null || !VALID_IMAGE_TYPES.contains(image.getContentType())) {
            throw new BadRequestException(ErrorMessage.INVALID_IMAGE_TYPE);
        }
        if (!new ImageSizeValidator().isValid(image, null)) {
            throw new BadRequestException(ErrorMessage.IMAGE_SIZE_EXCEEDED);
        }
    }
}