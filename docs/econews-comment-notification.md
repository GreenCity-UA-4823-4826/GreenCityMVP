# Notification on EcoNews Comment

Feature branch: `100-news-article-was-commented-notification`
GitHub issue: #100

## User story

> As a user I want to be notified when a news article I posted was commented so that I can reply soon.

**Preconditions:** a user posted a news article, another user leaves a comment on it.

**Acceptance criteria:**
- Leaving a top-level comment on someone else's article creates a notification for the article's author.
- The author's unread-notification counter increases by one.
- The notification popup / "All notifications" page shows:
  `[Username] commented on your news [News title]. [Date and Time]`
  - If 2+ users commented before the author viewed the notification, show the **last two** commenters' names joined with "and other users" (3+) or "and" (exactly 2).
  - If the same user comments more than once before being viewed, only the latest action is reflected (no duplicate entries).
  - `[News title]` should link to the article.
  - `[Date and Time]` uses `Today` / `Yesterday` / `DD.MM.YYYY`, `HH:MM XM`.

---

## Architecture overview

```
EcoNewsCommentServiceImpl.save()
        │  (parentCommentId == 0 && commenter != author)
        ▼
ApplicationEventPublisher.publishEvent(EcoNewsCommentNotificationEvent)
        │  (decoupled, in-process Spring event)
        ▼
EcoNewsCommentNotificationListener.onEcoNewsComment()   [@Async, @EventListener]
        │  (skips if commenter == author, defense in depth)
        ▼
UserNotificationService.createNotification(target, actionUser, type, targetId, newsTitle)
        │
        ├─ find existing UNVIEWED notification for (targetUser, ECONEWS_COMMENT, ecoNewsId)
        │      found  → merge action user into it, refresh time/title
        │      absent → build a brand-new Notification
        ▼
NotificationRepo.save(...)
        ▼
SimpMessagingTemplate.convertAndSend("/topic/{userId}/notification", unreadCount)
        (STOMP/WebSocket push of the updated unread-notification counter)
```

### Why an event instead of a direct call?

`EcoNewsCommentServiceImpl` publishes `EcoNewsCommentNotificationEvent` via Spring's
`ApplicationEventPublisher` rather than calling `UserNotificationService` directly.
`EcoNewsCommentNotificationListener` consumes it with `@EventListener` + `@Async`
(async execution enabled globally by `service/.../config/AsyncConfig.java` via
`@EnableAsync`). This means:
- Comment saving is never slowed down or failed by notification-building logic.
- The comment module has zero compile-time dependency on the notification module.

### Key files

| Layer | File | Responsibility |
|---|---|---|
| Event | `service-api/.../event/EcoNewsCommentNotificationEvent.java` | Immutable payload: author, commenter, ecoNewsId, newsTitle |
| Listener | `service/.../service/EcoNewsCommentNotificationListener.java` | Async consumer, skips self-comments, delegates to the notification service |
| Publisher | `service/.../service/EcoNewsCommentServiceImpl.java` | Publishes the event after a successful top-level comment save |
| Entity | `dao/.../entity/Notification.java` | JPA entity, `@ManyToMany actionUsers` (who acted), `@ManyToOne targetUser` (who's notified) |
| Repo | `dao/.../repository/NotificationRepo.java` | CRUD + `viewed`/`unread-count` queries |
| Service | `service/.../service/UserNotificationServiceImpl.java` | Merge logic, message templating, WebSocket push |
| DTOs | `service-api/.../dto/notification/{NotificationDto,ActionDto}.java` | API contract |
| Mapper | `service/.../mapping/NotificationDtoMapper.java` | `Notification` entity → `NotificationDto` (raw field copy) |
| Controller | `core/.../controller/NotificationController.java` | REST + STOMP endpoints |
| Resolver | `core/.../converters/UserIdArgumentResolver.java` | Resolves `@CurrentUserId Long userId` from the authenticated principal |
| i18n | `core/src/main/resources/notification{,_uk}.properties` | Message templates per locale |
| Migration | `dao/.../db/changelog/logs/ch-add-table-notification.xml` | `notifications` + `notifications_users` tables |
| Async | `service/.../config/AsyncConfig.java` | `@EnableAsync` — required for the `@Async` listener to actually run off-thread |
| WebSocket | `core/.../config/WebSocketConfig.java` | STOMP broker on `/topic`, endpoint `/socket` |

---

## Data model

```
notifications
  id, target_user_id, custom_message, target_id, second_message,
  second_message_id, notification_type, project_name, viewed, time, email_sent

notifications_users (join table, composite PK)
  notification_id, user_id   -- who is credited as an "action user" (commenter)
```

- `target_user_id` — the article author being notified.
- `notifications_users` — every distinct commenter that contributed to this
  (still-unread) notification, in the order they first/last acted.
- `second_message` — reused as the **news title** for `ECONEWS_COMMENT`
  notifications (field is generic so other notification types can repurpose it).
- `target_id` — the eco-news id, used to build the "link to article".

---

## Merge / dedup logic (`UserNotificationServiceImpl`)

```java
createNotification(targetUser, actionUser, notificationType, targetId, secondMessageText):
    notification = findExisting(targetUser.id, type, targetId, viewed = false)
                     .orElseGet(() -> new Notification(...))
    // add/replace actionUser, bump time, refresh secondMessage
    updateNotificationWithActionUser(notification, actionUser, secondMessageText)
    save + push unread count over WebSocket
```

- **Unviewed notification exists for the same article** → the new commenter is
  merged into `actionUsers` (dedup by user id: removed then re-added, so a
  repeat commenter moves to the "most recent" position instead of duplicating).
  The unread counter does **not** increase again — it's still one notification row.
- **No unviewed notification** (first comment, or the previous one was already
  viewed) → a fresh `Notification` row is created and the counter increases by one.
- `NotificationRepo.findNotificationByTargetUserIdAndNotificationTypeAndTargetIdAndViewedIsFalse`
  is the query backing "does an unviewed notification for this article already exist".

---

### Why "last two" works off list order

`updateNotificationWithActionUser` always does:

```java
notification.getActionUsers().removeIf(user -> user.getId().equals(actionUserId)); // no-op if new commenter
notification.getActionUsers().add(mappedActionUser);                                // appended at the END
```

So the list is self-maintaining in "least-recently-active → most-recently-active"
order — a repeat commenter is removed and re-appended, moving them to the end.
`subList(size-2, size)` therefore always yields the two most recent distinct
commenters, with no extra bookkeeping needed.

---

## What stayed backend-agnostic on purpose

- **Date/Time formatting** ("Today" / "Yesterday" / `DD.MM.YYYY HH:MM XM`) is
  **not** computed server-side. `NotificationDto.time` is sent as a raw
  `ZonedDateTime`; relative-day formatting depends on *when the client renders
  it* (not when the notification was created), so it belongs on the frontend
  with its own i18n/timezone handling.
- **"News title should be a link"** — the DTO exposes `targetId` (the eco-news
  id) and `secondMessage` (the title text); building the actual `<a href>` is
  a frontend concern.

---

## API surface (`NotificationController`)

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/notifications` | USER/ADMIN/MODERATOR/UBS_EMPLOYEE | Paged, filterable (`project-name`, `notification-types`, `viewed`) list for the current user |
| POST | `/notifications/{id}/viewNotification` | same | Mark as viewed, push updated unread count |
| POST | `/notifications/{id}/unreadNotification` | same | Mark as unread, push updated unread count |
| DELETE | `/notifications/{id}` | same | Delete (only if it belongs to the caller) |
| STOMP `/app/notifications` → broadcasts to `/topic/{userId}/notification` | — | Client-pushed unread-count refresh (e.g. on tab focus) |

`@CurrentUserId Long userId` (via `UserIdArgumentResolver`) and
`@ValidLanguage Locale locale` resolve the caller and locale automatically;
`ErrorMessage.NOTIFICATION_NOT_FOUND_BY_ID` / `NotFoundException` guard the
view/unread/delete operations against unknown or foreign notification ids.

---

## Test coverage added

| Test class | Module | Covers |
|---|---|---|
| `UserNotificationServiceImplTest` (15 tests) | service | filtering + templating (1/2/3+ commenters), merge vs. new-notification branching, dedup on repeat comment, delete/view/unread happy paths, ownership guard on delete/view/unread (`NotFoundException` for missing **and** foreign notification ids), WebSocket push payloads |
| `EcoNewsCommentNotificationListenerTest` (2) | service | event → `createNotification` delegation; self-comment is ignored |
| `EcoNewsCommentServiceImplTest` (+2 new, 25 total) | service | event is published when commenter ≠ author; **not** published when commenting on your own article |
| `NotificationDtoMapperTest` (1) | service | entity → DTO field mapping |
| `NotificationControllerTest` (7) | core | all 4 REST endpoints, `@CurrentUserId` resolution, happy-path response codes, plus 404 responses for view/unread/delete when the notification is missing or owned by another user (via `CustomExceptionHandler` wired into the standalone MockMvc setup) |

All 50 tests pass (`mvn test`, JDK 21 — see environment note below).

---
