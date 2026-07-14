package greencity.mapping.event;

import greencity.dto.event.EventCreateRequestDto;
import greencity.dto.event.EventDateDto;
import greencity.entity.User;
import greencity.entity.event.Event;
import greencity.entity.event.EventDate;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventCreateRequestDtoMapper {

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
            .map(this::toEventDate)
            .collect(Collectors.toList());
        eventDates.forEach(date -> date.setEvent(event));
        event.setDates(eventDates);

        return event;
    }

    private EventDate toEventDate(EventDateDto dateDto) {
        EventDate eventDate = new EventDate();
        eventDate.setDate(dateDto.getDate());
        eventDate.setStartTime(dateDto.getStartTime());
        eventDate.setEndTime(dateDto.getEndTime());
        eventDate.setAllDay(dateDto.isAllDay());
        return eventDate;
    }
}