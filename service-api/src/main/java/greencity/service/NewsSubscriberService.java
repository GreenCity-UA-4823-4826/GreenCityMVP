package greencity.service;

public interface NewsSubscriberService {
    void subscribe(String email);

    void unsubscribe(String token);
}
