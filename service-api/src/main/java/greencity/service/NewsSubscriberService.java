package greencity.service;

import greencity.dto.newssubscriber.NewsSubscriberResponseDto;
import java.util.List;

public interface NewsSubscriberService {
    void subscribe(String email);

    void unsubscribe(String token);

    List<NewsSubscriberResponseDto> findAllActiveSubscribers();
}
