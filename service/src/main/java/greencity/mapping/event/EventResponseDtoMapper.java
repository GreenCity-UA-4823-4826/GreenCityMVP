package greencity.mapping.event;

import greencity.dto.event.EventDateDto;
import greencity.dto.event.EventImageDto;
import greencity.dto.event.EventResponseDto;
import greencity.entity.event.Event;
import greencity.entity.event.EventDate;
import greencity.entity.event.EventImage;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventResponseDtoMapper extends AbstractConverter<Event, EventResponseDto> {
    @Override
    public EventResponseDto convert(Event event) {
        return EventResponseDto.builder()
            .id(event.getId())
            .title(event.getTitle())
            .description(event.getDescription())
            .organizerId(event.getOrganizer().getId())
            .organizerName(event.getOrganizer().getName())
            .eventTypes(event.getEventTypes())
            .initiativeTypes(event.getInitiativeTypes())
            .visibility(event.getVisibility())
            .location(event.getLocation())
            .latitude(event.getLatitude())
            .longitude(event.getLongitude())
            .onlineLink(event.getOnlineLink())
            .dates(mapDates(event.getDates()))
            .images(mapImages(event.getImages()))
            .createdAt(event.getCreatedAt())
            .build();
    }

    private List<EventDateDto> mapDates(List<EventDate> dates) {
        return dates.stream()
            .map(date -> {
                EventDateDto dto = new EventDateDto();
                dto.setDate(date.getDate());
                dto.setStartTime(date.getStartTime());
                dto.setEndTime(date.getEndTime());
                dto.setAllDay(date.isAllDay());
                return dto;
            })
            .collect(Collectors.toList());
    }

    private List<EventImageDto> mapImages(List<EventImage> images) {
        return images.stream()
            .map(image -> {
                EventImageDto dto = new EventImageDto();
                dto.setImageUrl(image.getImageUrl());
                dto.setMainImage(image.isMainImage());
                return dto;
            })
            .collect(Collectors.toList());
    }
}