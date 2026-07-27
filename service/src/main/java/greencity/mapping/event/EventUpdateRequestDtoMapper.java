package greencity.mapping.event;

import greencity.dto.event.EventUpdateRequestDto;
import greencity.entity.event.Event;
import org.springframework.stereotype.Component;

@Component
public class EventUpdateRequestDtoMapper {
    public void updateFields(EventUpdateRequestDto dto, Event event) {
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setEventTypes(dto.getEventTypes());
        event.setInitiativeTypes(dto.getInitiativeTypes());
        event.setVisibility(dto.getVisibility());
        event.setLocation(dto.getLocation());
        event.setLatitude(dto.getLatitude());
        event.setLongitude(dto.getLongitude());
        event.setOnlineLink(dto.getOnlineLink());
    }
}
