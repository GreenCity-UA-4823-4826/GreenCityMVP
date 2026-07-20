package greencity.service;

import greencity.application.event.EventCreatedNotificationEvent;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableDto;
import greencity.dto.event.EventCreateRequestDto;
import greencity.dto.event.EventResponseDto;
import greencity.dto.event.EventUpdateRequestDto;
import greencity.dto.event.MyEventResponseDto;
import greencity.dto.user.UserVO;
import greencity.entity.event.Event;
import greencity.entity.event.EventAttendance;
import greencity.entity.event.EventDate;
import greencity.entity.event.EventImage;
import greencity.enums.Role;
import greencity.application.event.EventDeletedNotificationEvent;
import greencity.application.event.EventUpdatedNotificationEvent;
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
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import greencity.entity.User;
import java.time.LocalDate;
import java.util.*;

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
    private final EventDateDtoMapper eventDateDtoMapper;
    private final EventUpdateRequestDtoMapper eventUpdateRequestDtoMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final ModelMapper modelMapper;

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

        eventPublisher.publishEvent(
                EventCreatedNotificationEvent.builder()
                        .organizer(userVO)
                        .eventId(savedEvent.getId())
                        .eventTitle(savedEvent.getTitle())
                        .build()
        );

        return eventResponseDtoMapper.convert(savedEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId, UserVO userVO) {
        Event event = eventRepo.findById(eventId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.EVENT_NOT_FOUND_BY_ID + eventId));

        validateUser(userVO, event);

        for (EventImage image : event.getImages()) {
            if (!image.getImageUrl().equals(AppConstant.DEFAULT_EVENT_IMAGE)) {
                fileService.delete(image.getImageUrl());
            }
        }

        List<UserVO> attendees = attendanceRepo.findByEventId(eventId).stream()
                .map(attendance -> modelMapper.map(attendance.getUser(), UserVO.class))
                .filter(attendee -> !attendee.getId().equals(userVO.getId()))
                .toList();
        String eventTitle = event.getTitle();
        if (!attendees.isEmpty()) {
            eventPublisher.publishEvent(
                    EventDeletedNotificationEvent.builder()
                            .organizer(userVO)
                            .attendees(attendees)
                            .eventId(eventId)
                            .eventTitle(eventTitle)
                            .build()
            );
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
            (int) Math.ceil((double) result.size() / pageable.getPageSize()));
    }

    @Override
    @Transactional
    public EventResponseDto updateEvent(Long eventId, EventUpdateRequestDto dto,
        MultipartFile[] newImages, UserVO userVO) {
        // find event
        Event event = eventRepo.findById(eventId)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.EVENT_NOT_FOUND_BY_ID + eventId));

        // permission check
        validateUser(userVO, event);

        // past event check
        boolean allPast = event.getDates().stream()
            .allMatch(d -> d.getDate().isBefore(LocalDate.now()));
        if (allPast) {
            throw new BadRequestException(ErrorMessage.EVENT_ALREADY_PASSED);
        }

        // validate image order
        if (dto.getImageOrder().size() > MAX_IMAGES) {
            throw new BadRequestException(ErrorMessage.TOO_MANY_EVENT_IMAGES);
        }
        long newImageMarkerCount = dto.getImageOrder().stream()
            .filter(item -> item.startsWith("NEW_"))
            .count();
        int actualNewImagesCount = (newImages == null) ? 0 : newImages.length;
        if (newImageMarkerCount != actualNewImagesCount) {
            throw new BadRequestException(ErrorMessage.IMAGE_MARKERS_COUNT_MISMATCH);
        }
        if (dto.getMainImageIndex() != null) {
            boolean indexOutOfBounds = dto.getMainImageIndex() < 0
                || dto.getMainImageIndex() >= dto.getImageOrder().size();
            if (indexOutOfBounds) {
                throw new BadRequestException(ErrorMessage.INVALID_MAIN_IMAGE_INDEX);
            }
        }

        // delete removed images from Azure
        List<String> currentUrls = event.getImages().stream()
            .map(EventImage::getImageUrl)
            .toList();
        currentUrls.stream()
            .filter(url -> !dto.getImageOrder().contains(url))
            .filter(url -> !url.equals(AppConstant.DEFAULT_EVENT_IMAGE))
            .forEach(fileService::delete);

        // upload new images to Azure
        Map<String, String> uploadedUrlsByMarker = new HashMap<>();
        for (String item : dto.getImageOrder()) {
            if (item.startsWith("NEW_") && newImages != null) {
                int fileIndex;
                try {
                    fileIndex = Integer.parseInt(item.substring("NEW_".length()));
                } catch (NumberFormatException e) {
                    throw new BadRequestException(ErrorMessage.IMAGE_MARKERS_COUNT_MISMATCH);
                }
                if (fileIndex < 0 || fileIndex >= newImages.length) {
                    throw new BadRequestException(ErrorMessage.IMAGE_MARKERS_COUNT_MISMATCH);
                }
                MultipartFile file = newImages[fileIndex];
                validateImage(file);
                String uploadedUrl = fileService.upload(file);
                uploadedUrlsByMarker.put(item, uploadedUrl);
            }
        }

        // build final image list
        List<EventImage> resultImages = new ArrayList<>();

        if (dto.getImageOrder().isEmpty()) {
            EventImage defaultImage = new EventImage();
            defaultImage.setImageUrl(AppConstant.DEFAULT_EVENT_IMAGE);
            defaultImage.setMainImage(true);
            defaultImage.setEvent(event);
            resultImages.add(defaultImage);
        } else {
            int mainIdx = (dto.getMainImageIndex() != null) ? dto.getMainImageIndex() : 0;

            for (int i = 0; i < dto.getImageOrder().size(); i++) {
                String item = dto.getImageOrder().get(i);

                if (!item.startsWith("NEW_") && !currentUrls.contains(item)) {
                    throw new BadRequestException(ErrorMessage.INVALID_IMAGE_URL);
                }

                String imageUrl = item.startsWith("NEW_")
                    ? uploadedUrlsByMarker.get(item)
                    : item;

                EventImage img = new EventImage();
                img.setImageUrl(imageUrl);
                img.setMainImage(i == mainIdx);
                img.setEvent(event);
                resultImages.add(img);
            }
        }
        event.getImages().clear();
        event.getImages().addAll(resultImages);

        // update scalar fields
        eventUpdateRequestDtoMapper.updateFields(dto, event);

        // update dates
        event.getDates().clear();
        List<EventDate> eventDates = dto.getDates().stream()
            .map(eventDateDtoMapper::toEntity)
            .toList();
        eventDates.forEach(d -> d.setEvent(event));
        event.getDates().addAll(eventDates);

        // save and return
        Event savedEvent = eventRepo.save(event);

        List<UserVO> attendees = attendanceRepo.findByEventId(eventId)
            .stream()
            .map(a -> modelMapper.map(a.getUser(), UserVO.class))
            .filter(a -> !a.getId().equals(userVO.getId()))
            .toList();

        if (!attendees.isEmpty()) {
            eventPublisher.publishEvent(EventUpdatedNotificationEvent.builder()
                .organizer(userVO)
                .attendees(attendees)
                .eventId(savedEvent.getId())
                .eventTitle(savedEvent.getTitle())
                .build());
        }

        return eventResponseDtoMapper.convert(savedEvent);
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

    private void validateUser(UserVO userVO, Event event) {
        if (userVO.getRole() != Role.ROLE_ADMIN
            && !userVO.getId().equals(event.getOrganizer().getId())) {
            throw new UserHasNoPermissionToAccessException(ErrorMessage.USER_HAS_NO_PERMISSION);
        }
    }
}