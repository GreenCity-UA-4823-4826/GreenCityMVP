package greencity.mapping.event;

import greencity.dto.event.EventDateDto;
import greencity.dto.event.EventPreviewResponseDto;
import greencity.entity.event.Event;
import greencity.entity.event.EventDate;
import greencity.entity.event.EventImage;
import greencity.enums.event.EventStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventPreviewResponseDtoMapper {
    public EventPreviewResponseDto toDto(Event event) {
        return EventPreviewResponseDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .organizerName(event.getOrganizer().getName())
                .eventTypes(event.getEventTypes())
                .mainImageUrl(resolveMainImageUrl(event.getImages()))
                .eventStatus(resolveEventStatus(event.getDates()))
                .visibility(event.getVisibility())
                .location(event.getLocation())
                .onlineLink(event.getOnlineLink())
                .dates(mapDates(event.getDates()))
                .build();
    }

    private EventStatus resolveEventStatus(List<EventDate> dates) {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        boolean hasUpcoming = false;

        for (EventDate eventDate : dates) {
            LocalDate eventDay = eventDate.getDate();

            if (eventDate.isAllDay()) {
                if (eventDay.equals(today)) {
                    return EventStatus.IN_LIVE;
                }
            } else {
                if (eventDay.equals(today)) {
                    LocalTime start = eventDate.getStartTime();
                    LocalTime end = eventDate.getEndTime();
                    if (!currentTime.isBefore(start) && !currentTime.isAfter(end)) {
                        return EventStatus.IN_LIVE;
                    }
                    if (currentTime.isBefore(start)) {
                        hasUpcoming = true;
                    }
                }
            }

            if (eventDay.isAfter(today)) {
                hasUpcoming = true;
            }
        }

        return hasUpcoming ? EventStatus.UPCOMING : EventStatus.PASSED;
    }

    private String resolveMainImageUrl(List<EventImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .filter(EventImage::isMainImage)
                .map(EventImage::getImageUrl)
                .findFirst()
                .orElse(images.get(0).getImageUrl());
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
}
