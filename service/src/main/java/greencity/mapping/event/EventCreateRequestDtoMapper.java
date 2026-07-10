package greencity.mapping.event;

import greencity.dto.event.EventCreateRequestDto;
import greencity.entity.User;
import greencity.entity.event.Event;
import greencity.entity.event.EventDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventCreateRequestDtoMapper {

    private final EventDateDtoMapper eventDateDtoMapper;

    public Event toEntity(EventCreateRequestDto dto, User organizer) {
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setOrganizer(organizer);
        event.setEventTypes(dto.getEventTypes());
        event.setInitiativeTypes(dto.getInitiativeTypes());
        event.setVisibility(dto.getVisibility());
        event.setLocation(dto.getLocation());
        event.setLatitude(dto.getLatitude());
        event.setLongitude(dto.getLongitude());
        event.setOnlineLink(dto.getOnlineLink());

        List<EventDate> eventDates = dto.getDates().stream()
                .map(eventDateDtoMapper::toEntity)
                .toList();
        eventDates.forEach(date -> date.setEvent(event));
        event.setDates(eventDates);

        return event;
    }
}