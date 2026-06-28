package greencity.dto.event;

import greencity.enums.event.EventType;
import greencity.enums.event.EventVisibility;
import greencity.enums.event.InitiativeType;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class EventResponseDto {

    private Long id;

    private String title;

    private String description;

    private Long organizerId;

    private String organizerName;

    private Set<EventType> eventTypes;

    private Set<InitiativeType> initiativeTypes;

    private EventVisibility visibility;

    private String location;
    private Double latitude;
    private Double longitude;

    private String onlineLink;

    private List<EventDateDto> dates;

    private List<EventImageDto> images;

    private LocalDateTime createdAt;
}