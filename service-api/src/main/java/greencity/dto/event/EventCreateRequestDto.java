package greencity.dto.event;

import greencity.annotations.ValidEventLocation;
import greencity.enums.event.EventType;
import greencity.enums.event.EventVisibility;
import greencity.enums.event.InitiativeType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@ValidEventLocation
@Getter
@Setter
public class EventCreateRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 70, message = "Title must not exceed 70 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 63206, message = "Description must be between 20 and 63206 characters")
    private String description;

    @NotEmpty(message = "At least one date is required")
    @Size(max = 7, message = "Maximum 7 dates allowed")
    @Valid
    private List<EventDateDto> dates;

    @NotEmpty(message = "At least one event type (PLACE or ONLINE) is required")
    private Set<EventType> eventTypes;

    @NotEmpty(message = "At least one initiative type is required")
    private Set<InitiativeType> initiativeTypes;

    private EventVisibility visibility = EventVisibility.OPEN;

    private String location;
    private Double latitude;
    private Double longitude;

    private String onlineLink;
}