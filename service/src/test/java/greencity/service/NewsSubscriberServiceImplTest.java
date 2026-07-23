package greencity.service;

import greencity.entity.NewsSubscriber;
import greencity.exception.exceptions.BadRequestException;
import greencity.repository.NewsSubscriberRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static greencity.constant.ErrorMessage.NEWS_SUBSCRIBER_EXIST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsSubscriberServiceImplTest {
    private static final String EMAIL = "user@example.com";

    @Mock
    private NewsSubscriberRepo newsSubscriberRepo;

    @InjectMocks
    private NewsSubscriberServiceImpl newsSubscriberService;

    @Test
    void subscribeSavesNewSubscriber() {
        when(newsSubscriberRepo.findNewsSubscriberByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());
        ArgumentCaptor<NewsSubscriber> captor = ArgumentCaptor.forClass(NewsSubscriber.class);

        newsSubscriberService.subscribe(EMAIL);

        verify(newsSubscriberRepo).save(captor.capture());
        assertEquals(EMAIL, captor.getValue().getEmail());
        assertNotNull(captor.getValue().getUnsubscribeToken());
        assertTrue(captor.getValue().isActive());
    }

    @Test
    void subscribeTrimsAndConvertsEmailToLowerCase() {
        when(newsSubscriberRepo.findNewsSubscriberByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());
        ArgumentCaptor<NewsSubscriber> captor = ArgumentCaptor.forClass(NewsSubscriber.class);

        newsSubscriberService.subscribe("  User@Example.COM  ");

        verify(newsSubscriberRepo).save(captor.capture());
        assertEquals(EMAIL, captor.getValue().getEmail());
    }

    @Test
    void subscribeRejectsExistingEmailIgnoringCase() {
        when(newsSubscriberRepo.findNewsSubscriberByEmailIgnoreCase(EMAIL))
            .thenReturn(Optional.of(NewsSubscriber.builder().email(EMAIL).build()));

        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> newsSubscriberService.subscribe("User@Example.COM"));

        assertEquals(NEWS_SUBSCRIBER_EXIST, exception.getMessage());
        verify(newsSubscriberRepo, never()).save(org.mockito.ArgumentMatchers.any(NewsSubscriber.class));
    }

    @Test
    void subscribeReactivatesInactiveSubscriber() {
        NewsSubscriber subscriber = NewsSubscriber.builder()
            .email(EMAIL)
            .unsubscribeToken("token")
            .active(false)
            .build();
        when(newsSubscriberRepo.findNewsSubscriberByEmailIgnoreCase(EMAIL))
            .thenReturn(Optional.of(subscriber));

        newsSubscriberService.subscribe(EMAIL);

        assertTrue(subscriber.isActive());
        verify(newsSubscriberRepo).save(subscriber);
    }

    @Test
    void unsubscribeMarksSubscriberAsInactive() {
        NewsSubscriber subscriber = NewsSubscriber.builder()
            .email(EMAIL)
            .unsubscribeToken("token")
            .active(true)
            .build();
        when(newsSubscriberRepo.findNewsSubscriberByUnsubscribeToken("token"))
            .thenReturn(Optional.of(subscriber));

        newsSubscriberService.unsubscribe("token");

        assertFalse(subscriber.isActive());
        verify(newsSubscriberRepo).save(subscriber);
        verify(newsSubscriberRepo, never()).delete(subscriber);
    }

    @Test
    void unsubscribeRejectsInvalidToken() {
        when(newsSubscriberRepo.findNewsSubscriberByUnsubscribeToken("invalid"))
            .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> newsSubscriberService.unsubscribe("invalid"));

        verify(newsSubscriberRepo, never()).save(org.mockito.ArgumentMatchers.any(NewsSubscriber.class));
    }

    @Test
    void findAllActiveSubscribersMapsRepositoryResult() {
        NewsSubscriber subscriber = NewsSubscriber.builder()
            .email(EMAIL)
            .unsubscribeToken("token")
            .active(true)
            .build();
        when(newsSubscriberRepo.findAllByActiveTrue()).thenReturn(List.of(subscriber));

        var result = newsSubscriberService.findAllActiveSubscribers();

        assertEquals(1, result.size());
        assertEquals(EMAIL, result.getFirst().getEmail());
        assertEquals("token", result.getFirst().getUnsubscribeToken());
    }
}
