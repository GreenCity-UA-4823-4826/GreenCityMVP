package greencity.dto.event;

import greencity.enums.event.EventAttendanceStatus;
import greencity.enums.event.EventStatus;
import greencity.enums.event.EventType;
import greencity.enums.event.EventVisibility;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
public class MyEventResponseDto {
    private EventAttendanceStatus attendanceStatus;

    private EventStatus eventStatus;

    private Long id;

    private String title;

    private String organizerName;

    private Set<EventType> eventTypes;

    private String mainImageUrl;

    private EventVisibility visibility;

    private String location;

    private String onlineLink;

    private List<EventDateDto> dates;
}
