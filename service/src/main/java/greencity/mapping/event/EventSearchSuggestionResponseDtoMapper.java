package greencity.mapping.event;

import greencity.dto.event.EventSearchSuggestionResponseDto;
import greencity.entity.event.Event;
import org.springframework.stereotype.Component;

@Component
public class EventSearchSuggestionResponseDtoMapper {
    public EventSearchSuggestionResponseDto toDto(Event event) {
        return EventSearchSuggestionResponseDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .build();
    }
}
