package greencity.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EventSearchSuggestionResponseDto {
    private Long id;

    private String title;
}
