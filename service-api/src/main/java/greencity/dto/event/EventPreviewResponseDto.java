package greencity.dto.event;

import greencity.enums.event.EventStatus;
import greencity.enums.event.EventType;
import greencity.enums.event.EventVisibility;
import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.Set;

@Getter
@Builder
public class EventPreviewResponseDto {
    private Long id;

    private String title;

    private String organizerName;

    private Set<EventType> eventTypes;

    private String mainImageUrl;

    private EventStatus eventStatus;

    private EventVisibility visibility;

    private String location;

    private String onlineLink;

    private List<EventDateDto> dates;
}
