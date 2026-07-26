# Notification on Event Like

Feature branch: `feature/105-event-was-liked-notification` (based on `feature/104-event-was-commented-notification`)
GitHub issue: none found in this repo's issue tracker — tracked externally as ticket #105.

## User story

> As an event organizer I want to be notified when someone likes my event so that I know it's resonating with the community.

**Preconditions:** a user organizes an event, another user likes it.

**Acceptance criteria:**
- Liking someone else's event creates/updates a notification for the event's organizer. Liking your own event never notifies you (self-like guard).
- If several *different* users like the same event before the organizer views the notification, the notification is aggregated: last two likers + "and other users" for 3+, same as comments.
- If the same user likes several *different* events before viewing, each event gets its **own** notification row (no cross-event aggregation), newest first.
- Unliking before the notification is viewed removes that user from the pending notification; if they were the only liker, the notification itself is deleted (the organizer should never see a notification for a like that no longer exists).
- The unread counter increases by exactly one per real like (even though likes on the same event aggregate into one notification row) and decreases back on unlike, before the notification is viewed.
- Only the organizer can view/mark/delete their own like notifications (inherited, unchanged, from #104's ownership checks).

---

## Architecture overview

```
EventLikeServiceImpl.like(eventId, user)  /  .unlike(eventId, user)
        │  (actor != organizer)
        ▼
ApplicationEventPublisher.publishEvent(EventLikeNotificationEvent{ liked: true|false })
        │  (decoupled, in-process Spring event)
        ▼
EventLikeNotificationListener.onEventLike()   [@Async, @TransactionalEventListener(AFTER_COMMIT)]
        │
        ├─ liked = true  → UserNotificationService.createNotification(organizer, liker, EVENT_LIKE, eventId, eventTitle)
        └─ liked = false → UserNotificationService.removeActionUser(organizer, liker, EVENT_LIKE, eventId)
                                  │
                                  ├─ no unread notification for this event        → no-op
                                  ├─ unread notification, other actors remain      → actor removed, notification saved
                                  └─ unread notification, actor was the only one   → notification deleted
        ▼
NotificationRepo.save(...) / .delete(...)
        ▼
SimpMessagingTemplate.convertAndSend("/topic/{userId}/notification", unreadActorCount)
```

`createNotification` (merge/dedup logic) is unchanged from #104/eco-news — see
[`docs/econews-comment-notification.md`](econews-comment-notification.md) for
the full merge-logic writeup. This document only covers what's new for likes:
the unlike/retraction path and the unread-counter semantics change.

### Key files

| Layer | File | Responsibility |
|---|---|---|
| Entity | `dao/.../entity/event/EventLike.java` | One row per `(event, user)` like, unique-constrained |
| Repo | `dao/.../repository/EventLikeRepo.java` | `existsByEventIdAndUserId`, `findByEventIdAndUserId`, `countByEventId` |
| Service | `service/.../service/EventLikeServiceImpl.java` | Idempotent like/unlike, organizer lazy-proxy re-fetch, publishes the event |
| Event | `service-api/.../event/EventLikeNotificationEvent.java` | Immutable payload: organizer, liker, eventId, eventTitle, `liked` flag |
| Listener | `service/.../service/EventLikeNotificationListener.java` | Async consumer; routes to create vs. remove based on `liked`; skips self-likes |
| Controller | `core/.../controller/EventLikeController.java` | `POST`/`DELETE /events/likes/{eventId}` |
| DTO | `service-api/.../dto/eventlike/EventLikeDtoResponse.java` | `{eventId, likesCount, liked}` response |
| Notification service | `service/.../service/UserNotificationServiceImpl.java` | `removeActionUser` (new), unread-actor counting (changed) |
| Migration | `dao/.../db/changelog/logs/ch-add-table-events-like.xml` | `events_like` table |
| i18n | `core/src/main/resources/notification{,_uk}.properties` | `EVENT_LIKE` / `EVENT_LIKE_TITLE` templates |

---

## Data model

```
events_like
  id, created_date, user_id, event_id
  UNIQUE (event_id, user_id)   -- a user can only like a given event once
  FK event_id -> events.id ON DELETE CASCADE
```

No changes to the `notifications` / `notifications_users` tables (see
`docs/econews-comment-notification.md`) — `EVENT_LIKE` is just another
`NotificationType` value using the same generic entity.

---

## New: retracting a notification on unlike (`removeActionUser`)

Comments have no "undo," so #104 never needed this. Likes do, per the spec's
edge case: *"a user removes their like — should the notification be deleted
or updated?"*

```java
removeActionUser(targetUser, actionUser, notificationType, targetId):
    notification = findExisting(targetUser.id, type, targetId, viewed = false)
    if absent: return                          // already viewed or never existed — nothing to retract
    notification.actionUsers.remove(actionUser)
    if notification.actionUsers.isEmpty(): delete notification
    else: save notification
    push updated unread count
```

This only touches **unread** notifications — once the organizer has viewed a
like notification, unliking afterwards does not retroactively un-notify them;
the notification is historical record at that point, same philosophy as the
existing viewed/unread split. The method is intentionally generic (not
like-specific) so any future notification type needing "undo" semantics can
reuse it.

---

## Changed: unread counter now counts actors, not rows

`GET /notifications/count` (and the same query backing the WebSocket push)
previously ran `countByTargetUserIdAndViewedIsFalse` — a plain count of
unread `Notification` rows. That undercounts once aggregation kicks in: if 3
different users like the same event before it's viewed, that's 3 real likes
merged into **1** notification row, so the counter read `1`, not `3`.

The spec for this feature requires the opposite: the counter must increase by
one per real like (and per distinct event liked by the same user), even
though they're aggregated for *display*. The fix, `NotificationRepo`:

```java
@Query("SELECT COUNT(u) FROM Notification n JOIN n.actionUsers u "
    + "WHERE n.targetUser.id = :userId AND n.viewed = false")
long countUnreadActionUsersByTargetUserId(Long userId);
```

This sums distinct `actionUsers` across a user's unread notifications instead
of counting rows. Because a user can only like a given event once (unique
constraint on `events_like`), one real like == exactly one `actionUsers`
entry, so:
- 3 users liking the same unread event → counter reads `3`.
- The same user liking 2 different events (2 separate notification rows) →
  counter reads `2`.
- Unliking before viewing removes that actor (`removeActionUser`, above),
  which automatically decrements the sum — no separate counter field needed.

This is a **shared** code path — it changes the counter for `EVENT_COMMENT`
(#104) too, not just `EVENT_LIKE`. That's intentional: `feature/104` isn't
merged yet, and the actor-based count is a more correct definition of "unread
count" for every aggregating notification type, not a like-specific special
case. If #104 had already shipped to production with the row-counting
behavior relied upon elsewhere, this would need a migration/rollout plan
instead of a direct swap.

---

## What stayed backend-agnostic on purpose

Same as #104/eco-news: date/time formatting and "event title as a link" are
left to the frontend. See `docs/econews-comment-notification.md`.

---

## API surface (`EventLikeController`)

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/events/likes/{eventId}` | USER/ADMIN/MODERATOR/UBS_EMPLOYEE | Like an event (idempotent — already-liked is a no-op, still returns current state) |
| DELETE | `/events/likes/{eventId}` | USER/ADMIN/MODERATOR/UBS_EMPLOYEE | Unlike an event (idempotent — not-liked is a no-op) |

Both return `EventLikeDtoResponse { eventId, likesCount, liked }`. No separate
"get like status" endpoint exists yet — the like/unlike response doubles as
the current state.

Notification retrieval/view/unread/delete/count reuse `NotificationController`
unchanged (see `docs/econews-comment-notification.md`); `EVENT_LIKE` is just
another value accepted by the `notification-types` filter.

---

## Test coverage added

| Test class | Module | Covers |
|---|---|---|
| `EventLikeServiceImplTest` (8 tests) | service | like/unlike happy paths, self-like guard, already-liked idempotency, event-not-found, organizer lazy-proxy re-fetch regression |
| `EventLikeNotificationListenerTest` (3) | service | like → `createNotification`, unlike → `removeActionUser`, self-like is ignored entirely |
| `EventLikeControllerTest` (2) | core | both REST endpoints, response codes |
| `UserNotificationServiceImplTest` (+3 new) | service | `removeActionUser`: removes-and-saves, empties-and-deletes, no-op when nothing to retract |

All new/changed tests pass locally; the two pre-existing unrelated failures
(`UserVOMapperTest`, and `LanguageRepoTest` which needs Docker/Testcontainers)
are the same ones already called out on #104 and are unaffected by this
branch.
