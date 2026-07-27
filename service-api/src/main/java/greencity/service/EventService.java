package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.event.EventCreateRequestDto;
import greencity.dto.event.EventResponseDto;
import greencity.dto.event.EventUpdateRequestDto;
import greencity.dto.event.MyEventResponseDto;
import greencity.dto.event.EventPreviewResponseDto;
import greencity.dto.event.EventSearchSuggestionResponseDto;
import greencity.dto.user.UserVO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Provides the interface to manage {@code Event} entity.
 */
public interface EventService {
    /**
     * Method for creating a new Event.
     *
     * @param eventCreateRequestDto - dto with event data.
     * @param images                - array of images for the event, can be null.
     * @param mainImageIndex        - index of the main image in the images array,
     *                              can be null.
     * @param userVO                - current authorized user who organizes the
     *                              event.
     * @return created event as {@link EventResponseDto}.
     */
    EventResponseDto createEvent(EventCreateRequestDto eventCreateRequestDto,
        MultipartFile[] images,
        Integer mainImageIndex,
        UserVO userVO);

    /**
     * Method for deleting an Event by id. Only the event organizer or admin can
     * delete it.
     *
     * @param eventId - id of the event to delete.
     * @param userVO  - current authorized user performing the deletion.
     */
    void deleteEvent(Long eventId, UserVO userVO);

    /**
     * Method for getting all events the user joined or scheduled.
     *
     * @param userVO   - current authorized user.
     * @param pageable - pagination parameters.
     * @return page of {@link MyEventResponseDto}.
     */
    PageableDto<MyEventResponseDto> getMyEvents(UserVO userVO, Pageable pageable);

    /**
     * Method for getting event title suggestions for search dropdown.
     *
     * @param query - search string (minimum 3 characters).
     * @return list of {@link EventSearchSuggestionResponseDto} with matching event
     *         titles.
     */
    List<EventSearchSuggestionResponseDto> getSearchSuggestions(String query);

    /**
     * Method for searching events by title.
     *
     * @param query - search string (minimum 3 characters).
     * @return {@link Page} of {@link EventPreviewResponseDto} sorted by relevance.
     */
    Page<EventPreviewResponseDto> searchEvents(String query, Pageable pageable);

    /**
     * Method for updating an existing Event. Only the event organizer or admin can
     * edit it.
     *
     * @param eventId   - id of the event to update.
     * @param dto       - dto with updated event data including image order.
     * @param newImages - array of new images to upload, can be null.
     * @param userVO    - current authorized user performing the update.
     * @return updated event as {@link EventResponseDto}.
     */
    EventResponseDto updateEvent(Long eventId, EventUpdateRequestDto dto,
        MultipartFile[] newImages, UserVO userVO);
}