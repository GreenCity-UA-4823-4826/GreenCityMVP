package greencity.dto.newssubscriber;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsSubscriberResponseDto {
    private String email;
    private String unsubscribeToken;
}
