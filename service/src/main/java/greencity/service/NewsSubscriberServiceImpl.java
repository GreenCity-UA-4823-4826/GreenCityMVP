package greencity.service;

import greencity.entity.NewsSubscriber;
import greencity.repository.NewsSubscriberRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NewsSubscriberServiceImpl implements NewsSubscriberService {
    private final NewsSubscriberRepo newsSubscriberRepo;

    @Override
    public void subscribe(String email) {
        NewsSubscriber subscriber = NewsSubscriber.builder()
            .email(email)
            .unsubscribeToken(UUID.randomUUID().toString())
            .build();

        if (newsSubscriberRepo.findNewsSubscriberByEmail(email).isPresent()) {
            return;
        } else {
            newsSubscriberRepo.save(subscriber);
        }
    }

    @Override
    public void unsubscribe(String token) {  //ToDo implement the unsubscribe logic (Frontend)
        NewsSubscriber subscriber = newsSubscriberRepo
            .findNewsSubscriberByUnsubscribeToken(token)
            .orElseThrow(() -> new IllegalArgumentException(
                "Subscriber or unsubscribe token is invalid"));

        newsSubscriberRepo.delete(subscriber);
    }
}
