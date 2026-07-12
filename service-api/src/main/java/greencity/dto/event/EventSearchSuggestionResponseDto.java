package greencity.dto.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventSearchSuggestionResponseDto {
    private Long id;

    private String title;
}
