package greencity.controller;

import greencity.dto.newssubscriber.NewsSubscriberRequestDto;
import greencity.service.NewsSubscriberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/news-subscribers")
@RequiredArgsConstructor
public class NewsSubscriberController {
    private final NewsSubscriberService newsSubscriberService;

    @PostMapping
    public ResponseEntity<Void> subscribe(
        @Valid @RequestBody NewsSubscriberRequestDto request) {
        newsSubscriberService.subscribe(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<Void> unsubscribe(@PathVariable String token) {
        newsSubscriberService.unsubscribe(token);
        return ResponseEntity.noContent().build();
    }

}
