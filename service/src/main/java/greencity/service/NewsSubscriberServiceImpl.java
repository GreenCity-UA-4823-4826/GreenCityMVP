package greencity.service;

import greencity.entity.NewsSubscriber;
import greencity.dto.newssubscriber.NewsSubscriberResponseDto;
import greencity.exception.exceptions.BadRequestException;
import greencity.repository.NewsSubscriberRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Locale;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static greencity.constant.ErrorMessage.NEWS_SUBSCRIBER_EXIST;

@Service
@RequiredArgsConstructor
public class NewsSubscriberServiceImpl implements NewsSubscriberService {
    private final NewsSubscriberRepo newsSubscriberRepo;

    @Override
    public void subscribe(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        Optional<NewsSubscriber> existingSubscriber =
            newsSubscriberRepo.findNewsSubscriberByEmailIgnoreCase(normalizedEmail);

        if (existingSubscriber.isPresent()) {
            NewsSubscriber subscriber = existingSubscriber.get();
            if (subscriber.isActive()) {
                throw new BadRequestException(NEWS_SUBSCRIBER_EXIST);
            }
            subscriber.setActive(true);
            newsSubscriberRepo.save(subscriber);
            return;
        }

        newsSubscriberRepo.save(NewsSubscriber.builder()
            .email(normalizedEmail)
            .unsubscribeToken(UUID.randomUUID().toString())
            .active(true)
            .build());
    }

    @Override
    public void unsubscribe(String token) {
        NewsSubscriber subscriber = newsSubscriberRepo
            .findNewsSubscriberByUnsubscribeToken(token)
            .orElseThrow(() -> new IllegalArgumentException(
                "Subscriber or unsubscribe token is invalid"));

        subscriber.setActive(false);
        newsSubscriberRepo.save(subscriber);
    }

    @Override
    public List<NewsSubscriberResponseDto> findAllActiveSubscribers() {
        return newsSubscriberRepo.findAllByActiveTrue().stream()
            .map(subscriber -> new NewsSubscriberResponseDto(
                subscriber.getEmail(), subscriber.getUnsubscribeToken()))
            .toList();
    }
}
