# Notification on Event Comment

Feature branch: `feature/104-event-was-commented-notification`
GitHub issue: none found in this repo's issue tracker — tracked externally as ticket #104.

## User story

> As an event organizer I want to be notified when someone comments on my event so that I can reply soon.

**Preconditions:** a user organizes an event, another user leaves a comment on it. Events had no comment feature at all before this branch — it's built from scratch here, alongside the notification.

**Acceptance criteria:**
- Leaving a comment on someone else's event creates a notification for the event's organizer. Commenting on your own event never notifies you (self-comment guard).
- The organizer's unread-notification counter increases by one.
- The notification popup / "All notifications" page shows:
  `[Username] commented on your event [Event title]. [Date and Time]`
  - If 2+ users commented before the organizer viewed the notification, show the **last two** commenters' names joined with "and other users" (3+) or "and" (exactly 2).
  - If the same user comments more than once before being viewed, only the latest action is reflected (no duplicate entries).
  - `[Date and Time]` uses `Today` / `Yesterday` / `DD.MM.YYYY`, `HH:MM XM` — computed client-side, see below.
- Only the organizer can view/mark/delete their own comment notifications; a caller cannot act on someone else's notification by guessing its id (fixed on this branch, see below).

---

## Architecture overview

```
EventCommentServiceImpl.save(eventId, request, commenter)
        │  (commenter != organizer)
        ▼
ApplicationEventPublisher.publishEvent(EventCommentNotificationEvent)
        │  (decoupled, in-process Spring event)
        ▼
EventCommentNotificationListener.onEventComment()   [@Async, @TransactionalEventListener(AFTER_COMMIT)]
        │  (skips if commenter == organizer, defense in depth)
        ▼
UserNotificationService.createNotification(organizer, commenter, EVENT_COMMENT, eventId, eventTitle)
        │
        ├─ find existing UNVIEWED notification for (organizer, EVENT_COMMENT, eventId)
        │      found  → merge commenter into it, refresh time/title
        │      absent → build a brand-new Notification
        ▼
NotificationRepo.save(...)
        ▼
SimpMessagingTemplate.convertAndSend("/topic/{userId}/notification", unreadCount)
```

This reuses the exact merge/dedup/aggregation logic already documented for
eco-news comments in
[`docs/econews-comment-notification.md`](econews-comment-notification.md) —
`UserNotificationServiceImpl`, `Notification`/`NotificationRepo`, the
`NotificationDto`/`NotificationController` API surface, and the "last two
commenters" list-ordering trick are all untouched by this branch. This
document only covers what's new: the event-comment feature itself, and two
fixes to the shared notification code discovered while building it.

### Why `@TransactionalEventListener(AFTER_COMMIT)` instead of `@EventListener`

Unlike the eco-news listener (plain `@EventListener`), this listener fires
only **after** the enclosing `@Transactional` comment-save commits. This
avoids building a notification for a comment that ends up rolled back (e.g.
a later validation failure in the same transaction), at the cost of the
notification lagging slightly behind the HTTP response by however long commit
takes.

### Key files

| Layer | File | Responsibility |
|---|---|---|
| Entity | `dao/.../entity/event/EventComment.java` | Comment text, author, event, `@CreatedDate` |
| Repo | `dao/.../repository/EventCommentRepo.java` | Paged fetch by event (newest first), `existsByIdAndUserId` for delete-ownership |
| Service | `service/.../service/EventCommentServiceImpl.java` | Save/list/delete; publishes the notification event; re-fetches the organizer to avoid a lazy-proxy pitfall (below) |
| Event | `service-api/.../event/EventCommentNotificationEvent.java` | Immutable payload: organizer, commenter, eventId, eventTitle |
| Listener | `service/.../service/EventCommentNotificationListener.java` | Async, after-commit consumer; skips self-comments; delegates to `UserNotificationService` |
| Controller | `core/.../controller/EventCommentController.java` | `POST`/`GET /events/comments/{eventId}`, `DELETE /events/comments/{eventCommentId}` |
| DTOs | `service-api/.../dto/eventcomment/{AddEventCommentDtoRequest,AddEventCommentDtoResponse,EventCommentAuthorDto,EventCommentDto}.java` | API contract |
| Mapper | `service/.../mapping/EventCommentDtoMapper.java` | `EventComment` entity → `EventCommentDto` |
| Migration | `dao/.../db/changelog/logs/ch-add-table-events-comment.xml` | `events_comment` table, FKs to `users`/`events` |
| Notification service | `service/.../service/UserNotificationServiceImpl.java` | Ownership checks on view/unread (changed, see below) |
| Notification controller | `core/.../controller/NotificationController.java` | Adds `GET /notifications/count` (new, see below) |
| i18n | `core/src/main/resources/notification{,_uk}.properties` | `EVENT_COMMENT` / `EVENT_COMMENT_TITLE` templates |

---

## Data model

```
events_comment
  id, text, created_date, user_id, event_id
  FK event_id -> events.id ON DELETE CASCADE
  FK user_id  -> users.id
```

No changes to the `notifications` / `notifications_users` tables — see
`docs/econews-comment-notification.md`. `EVENT_COMMENT` is simply another
`NotificationType` value using the same generic `Notification` entity;
`target_id` is the event id and `second_message` is reused as the event
title, exactly as `ECONEWS_COMMENT` reuses it for the news title.

---

## Gotcha: `Event.organizer` is a lazy proxy

`EventCommentServiceImpl.save()` needs to map the organizer to a `UserVO` to
build the notification event's payload. `event.getOrganizer()` returns a
Hibernate lazy proxy — reading its `id` doesn't force initialization, but
`ModelMapper` (configured for private-field access) maps an uninitialized
proxy to an all-null `UserVO` instead of throwing. The fix is to re-load the
organizer through `UserRepo` before mapping:

```java
Long organizerId = event.getOrganizer() == null ? null : event.getOrganizer().getId();
if (organizerId != null && !userVO.getId().equals(organizerId)) {
    User organizer = userRepo.findById(organizerId)
        .orElseThrow(...);
    eventPublisher.publishEvent(EventCommentNotificationEvent.builder()
        .organizer(modelMapper.map(organizer, UserVO.class))
        ...
```

Covered by a regression test (`save_CommenterIsNotOrganizer_LoadsOrganizerInsteadOfMappingLazyProxy`)
that asserts `userRepo.findById(organizerId)` is actually called rather than
mapping the proxy directly.

---

## Fixed while building this: notification view/unread ownership check

`viewNotification`/`unreadNotification` previously only checked that a
notification existed (`findById`), not that it belonged to the calling user —
any authenticated user could mark someone else's notification as
viewed/unread by guessing its id. Both methods now scope the update to the
caller via `NotificationRepo.markNotificationAsViewedByIdAndTargetUserId` /
`markNotificationAsNotViewedByIdAndTargetUserId` (a `WHERE ... AND
target_user_id = :targetUserId` update), throwing `NotFoundException` (404)
when zero rows were affected — the same protection `deleteNotification`
already had via `existsByIdAndTargetUserId`.

## Added while building this: `GET /notifications/count`

`NotificationController` gained `GET /notifications/count`, returning
`userNotificationService.countUnreadNotifications(userId)`, so the client can
fetch the unread badge count on initial page load instead of only receiving
it on the next WebSocket push.

> Both of the above are the foundation `feature/105-event-was-liked-notification`
> builds on top of — see
> [`docs/event-like-notification.md`](event-like-notification.md), which
> changes what `countUnreadNotifications` actually counts (rows → actors) and
> adds the corresponding "retract a notification" method for unlike.

---

## What stayed backend-agnostic on purpose

Same as eco-news comments: date/time formatting ("Today" / "Yesterday" /
`DD.MM.YYYY HH:MM XM`) is not computed server-side — `NotificationDto.time`
is a raw `ZonedDateTime`, and relative-day formatting is a frontend concern
since it depends on render time, not creation time. "Event title as a link"
similarly just needs `targetId` + `secondMessage` from the DTO; building the
`<a href>` is frontend's job.

---

## API surface

### `EventCommentController`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/events/comments/{eventId}` | USER/ADMIN/MODERATOR/UBS_EMPLOYEE | Add a comment, publishes the notification event |
| GET | `/events/comments/{eventId}` | USER/ADMIN/MODERATOR/UBS_EMPLOYEE | Paged list of comments, newest first |
| DELETE | `/events/comments/{eventCommentId}` | author or ADMIN | Delete a comment |

### `NotificationController` (unchanged endpoints + one addition)

See `docs/econews-comment-notification.md` for the full table; this branch
adds:

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/notifications/count` | USER/ADMIN/MODERATOR/UBS_EMPLOYEE | Current user's unread-notification count |

---

## Test coverage added

| Test class | Module | Covers |
|---|---|---|
| `EventCommentServiceImplTest` (10 tests) | service | save publishes/skips notification event (self-comment guard), lazy-proxy organizer re-fetch regression, not-found paths, list/delete (author vs. admin vs. neither) |
| `EventCommentNotificationListenerTest` (2) | service | event → `createNotification` delegation; self-comment is ignored |
| `EventCommentControllerTest` (3) | core | all 3 REST endpoints, response codes |
| `UserNotificationServiceImplTest` (updated) | service | ownership-check fix on view/unread, `NotFoundException` on foreign/missing ids |
| `NotificationControllerTest` (updated) | core | new `GET /notifications/count` endpoint |

All new/changed tests pass locally; `LanguageRepoTest` (Testcontainers/Docker)
could not be run in this environment (no Docker available) — the migration
was validated statically instead (well-formed XML, unique changeset ids,
correct FK ordering relative to `events`).
