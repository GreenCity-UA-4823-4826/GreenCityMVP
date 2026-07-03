package greencity.controller;

import greencity.annotations.CurrentUser;
import greencity.constant.HttpStatuses;
import greencity.dto.event.EventCreateRequestDto;
import greencity.dto.event.EventResponseDto;
import greencity.dto.event.MyEventResponseDto;
import greencity.dto.user.UserVO;
import greencity.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    /**
     * Method for creating a new {@link EventResponseDto} for the current
     * authorized user.
     *
     * @param eventCreateRequestDto {@link EventCreateRequestDto} with event data.
     * @param images                array of event images, can be null.
     * @param mainImageIndex        index of the main image in the images array, can be null.
     * @param user                  {@link UserVO} current authorized user who organizes the event.
     * @return {@link ResponseEntity}.
     */
    @Operation(summary = "Create new event.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
                    content = @Content(schema = @Schema(implementation = EventResponseDto.class))),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<EventResponseDto> createEvent(
            @Parameter(description = "Event data")
            @Valid @RequestPart EventCreateRequestDto eventCreateRequestDto,
            @Parameter(description = "Event images, max 5")
            @RequestPart(required = false) MultipartFile[] images,
            @Parameter(description = "Index of the main image in the images array")
            @RequestParam(required = false) Integer mainImageIndex,
            @Parameter(hidden = true) @CurrentUser UserVO user) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(eventCreateRequestDto, images, mainImageIndex, user));
    }

    /**
     * Method for getting all events the current user joined or scheduled.
     *
     * @param userVO   - current authorized user.
     * @param pageable - pagination parameters.
     * @return {@link ResponseEntity} with page of {@link MyEventResponseDto}.
     */
    @Operation(summary = "Get current user's events")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
                    content = @Content(schema = @Schema(implementation = MyEventResponseDto.class))),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/myEvents")
    public ResponseEntity<Page<MyEventResponseDto>> getMyEvents(
            @Parameter(hidden = true) @CurrentUser UserVO userVO,
            Pageable pageable) {

        return ResponseEntity.ok(eventService.getMyEvents(userVO, pageable));
    }

    /**
     * Method for deleting an Event by id.
     * Only the event organizer or admin can perform this action.
     *
     * @param eventId - id of the event to delete.
     * @param userVO  - current authorized user performing the deletion.
     * @return {@link ResponseEntity} with status 200.
     */
    @Operation(summary = "Delete event by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId,
                                              @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        eventService.deleteEvent(eventId, userVO);
        return ResponseEntity.ok().build();
    }
}