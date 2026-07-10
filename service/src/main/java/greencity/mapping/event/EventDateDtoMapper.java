package greencity.mapping.event;

import greencity.dto.event.EventDateDto;
import greencity.entity.event.EventDate;
import org.springframework.stereotype.Component;

@Component
public class EventDateDtoMapper {
    public EventDate toEntity(EventDateDto dto) {
        EventDate eventDate = new EventDate();
        eventDate.setDate(dto.getDate());
        eventDate.setStartTime(dto.getStartTime());
        eventDate.setEndTime(dto.getEndTime());
        eventDate.setAllDay(dto.isAllDay());
        return eventDate;
    }
}
