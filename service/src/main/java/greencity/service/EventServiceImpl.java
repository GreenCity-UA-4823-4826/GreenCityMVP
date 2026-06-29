package greencity.service;

import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.event.EventCreateRequestDto;
import greencity.dto.event.EventResponseDto;
import greencity.dto.user.UserVO;
import greencity.entity.User;
import greencity.entity.event.Event;
import greencity.entity.event.EventImage;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.mapping.event.EventCreateRequestDtoMapper;
import greencity.mapping.event.EventResponseDtoMapper;
import greencity.repository.EventRepo;
import greencity.repository.UserRepo;
import greencity.validator.ImageSizeValidator;

import lombok.RequiredArgsConstructor;

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

    @Override
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
            eventImage.setMainImage(mainImageIndex != null && mainImageIndex == i);

            eventImages.add(eventImage);
        }

        return eventImages;
    }

    private void validateImage(MultipartFile image) {
        if (image.getContentType() == null || !VALID_IMAGE_TYPES.contains(image.getContentType())) {
            throw new BadRequestException(ErrorMessage.IMAGE_EXISTS);
        }
        if (!new ImageSizeValidator().isValid(image, null)) {
            throw new BadRequestException(ErrorMessage.IMAGE_SIZE_EXCEEDED);
        }
    }
}