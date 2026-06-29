package greencity.service;

import greencity.dto.event.EventCreateRequestDto;
import greencity.dto.event.EventResponseDto;
import greencity.dto.user.UserVO;

import org.springframework.web.multipart.MultipartFile;

/**
 * Provides the interface to manage {@code Event} entity.
 */
public interface EventService {

    /**
     * Method for creating a new Event.
     *
     * @param eventCreateRequestDto - dto with event data.
     * @param images                - array of images for the event, can be null.
     * @param mainImageIndex        - index of the main image in the images array, can be null.
     * @param userVO                - current authorized user who organizes the event.
     * @return created event as {@link EventResponseDto}.
     */
    EventResponseDto createEvent(EventCreateRequestDto eventCreateRequestDto,
                                 MultipartFile[] images,
                                 Integer mainImageIndex,
                                 UserVO userVO);
}