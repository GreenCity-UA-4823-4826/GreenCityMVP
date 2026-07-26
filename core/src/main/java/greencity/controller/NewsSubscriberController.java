package greencity.controller;

import greencity.dto.newssubscriber.NewsSubscriberRequestDto;
import greencity.service.NewsSubscriberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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

    @GetMapping(value = "/unsubscribe", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> unsubscribeByLink(@RequestParam String token) {
        newsSubscriberService.unsubscribe(token);
        return ResponseEntity.ok("""
            <!doctype html>
            <html lang="en">
            <head><meta charset="UTF-8"><title>GreenCity newsletter</title></head>
            <body><h1>You have successfully unsubscribed from GreenCity Eco-news.</h1></body>
            </html>
            """);
    }

}
