# Notification on Habit Like

Feature branch: `feature/111-habit-was-liked-notification` (based on `feature/105-event-was-liked-notification`)
GitHub issue: none found in this repo's issue tracker — tracked externally as ticket #111.

## User story

> As a habit owner I want to be notified when someone likes my habit so that I know it's resonating with the community.

**Preconditions:** a user creates a custom habit, another user likes it. Habits had no like feature at all before this branch — it's built from scratch here, alongside the notification.

**Acceptance criteria:**
- Liking someone else's habit creates/updates a notification for the habit's owner. Liking your own habit never notifies you (self-like guard).
- Default/global habits (not custom-created by any single user) have no owner at all — liking them never triggers a notification, since there's no one to notify. Same ownership model as [`docs/habit-comment-notification.md`](habit-comment-notification.md) (#110).
- If several *different* users like the same habit before the owner views the notification, the notification is aggregated: last two likers + "and other users" for 3+.
- If the same user likes several *different* habits before viewing, each habit gets its **own** notification row (no cross-habit aggregation), newest first.
- Unliking before the notification is viewed removes that user from the pending notification; if they were the only liker, the notification itself is deleted.
- The unread counter increases by exactly one per real like (even though likes on the same habit aggregate into one notification row) and decreases back on unlike, before the notification is viewed — the **actor-based** counter introduced in #105, not the row-based one from #110.
- Only the owner can view/mark/delete their own like notifications; a user can only like a given habit once (unique constraint).

---

## Why this branch is off #105, not #110

This ticket needs two things #105 already built and #110 deliberately does not have:

1. **Actor-based unread counting** — this spec requires the counter to increase per individual like, even when merged into one aggregated notification row (e.g. 3 users liking the same unread habit → counter reads `3`). #105 changed the shared `NotificationRepo` query from counting unread rows to summing distinct `actionUsers` across unread notifications specifically for this reason. #110 (habit comments) intentionally kept the original row-based counting, because *its* spec asked for the opposite. Branching from #110 would mean re-deriving #105's actor-based change from scratch, or living with the wrong counter semantics.
2. **`removeActionUser`** — the generic "retract an actor from an unread notification, delete it if empty" method #105 added to `UserNotificationService` for the unlike edge case. Comments have no "undo," so #110 never needed it; likes do.

`feature/110-habit-was-commented-notification` (habit *comments*) is unrelated to this feature and isn't needed here at all — this branch does not include it.

---

## Habit ownership: same model as #110

Unlike `Event.organizer` (`@ManyToOne User`), `Habit` only has a raw
`@Column(name = "user_id") private Long userId;`
(`dao/src/main/java/greencity/entity/Habit.java:37-38`), populated **only
when a custom habit is created**. Default/global habits leave it `null` — no
single owner by design. `HabitLikeServiceImpl` reads `habit.getUserId()`
directly (a plain `Long`, no lazy-proxy re-fetch needed) and skips publishing
the notification entirely when it's `null`, or when it equals the liker's own
id — identical treatment to `HabitCommentServiceImpl` in #110.

---

## Architecture overview

```text
HabitLikeServiceImpl.like(habitId, user)  /  .unlike(habitId, user)
        │  (liker != owner && habit.userId != null)
        ▼
ApplicationEventPublisher.publishEvent(HabitLikeNotificationEvent{ liked: true|false })
        │  (decoupled, in-process Spring event)
        ▼
HabitLikeNotificationListener.onHabitLike()   [@Async, @TransactionalEventListener(AFTER_COMMIT)]
        │
        ├─ liked = true  → UserNotificationService.createNotification(owner, liker, HABIT_LIKE, habitId, habitName)
        └─ liked = false → UserNotificationService.removeActionUser(owner, liker, HABIT_LIKE, habitId)
                                  │
                                  ├─ no unread notification for this habit           → no-op
                                  ├─ unread notification, other actors remain        → actor removed, notification saved
                                  └─ unread notification, actor was the only one     → notification deleted
        ▼
NotificationRepo.save(...) / .delete(...)
        ▼
SimpMessagingTemplate.convertAndSend("/topic/{userId}/notification", unreadActorCount)
```

This is a direct mirror of `EventLikeServiceImpl`/`EventLikeNotificationListener`
(#105) — see [`docs/event-like-notification.md`](event-like-notification.md)
for the full writeup of the aggregation/unlike/counter mechanics, which are
entirely unchanged here. This document only covers what's specific to habits:
the ownership model and habit-name resolution.

### Resolving the habit's display name

Same as #110: `Habit` has no plain `title` field, only per-language
`HabitTranslation` rows. The notification's `secondMessage` is resolved via
`HabitTranslationRepo.findByHabitAndLanguageCode(habit, AppConstant.DEFAULT_LANGUAGE_CODE)`
(`"en"`), falling back to `null` if no English translation exists.

### Key files

| Layer | File | Responsibility |
|---|---|---|
| Entity | `dao/.../entity/HabitLike.java` | One row per `(habit, user)` like, unique-constrained |
| Repo | `dao/.../repository/HabitLikeRepo.java` | `existsByHabitIdAndUserId`, `findByHabitIdAndUserId`, `countByHabitId` |
| Service | `service/.../service/HabitLikeServiceImpl.java` | Idempotent like/unlike, owner-null/self-like guards, habit-name resolution, publishes the event |
| Event | `service-api/.../event/HabitLikeNotificationEvent.java` | Immutable payload: owner, liker, habitId, habitName, `liked` flag |
| Listener | `service/.../service/HabitLikeNotificationListener.java` | Async, after-commit consumer; routes to create vs. remove based on `liked`; skips self-likes |
| Controller | `core/.../controller/HabitLikeController.java` | `POST`/`DELETE /habit/likes/{habitId}` |
| DTO | `service-api/.../dto/habitlike/HabitLikeDtoResponse.java` | `{habitId, likesCount, liked}` response |
| Migration | `dao/.../db/changelog/logs/ch-add-table-habit-like.xml` | `habit_like` table |
| i18n | `core/src/main/resources/notification{,_uk}.properties` | `HABIT_LIKE` / `HABIT_LIKE_TITLE` templates |

No changes to `UserNotificationService`/`UserNotificationServiceImpl`,
`NotificationController`, `NotificationDto`, or the ownership-check logic —
`removeActionUser` and the actor-based counter are fully generic and reused
as-is from #105.

---

## Data model

```text
habit_like
  id, created_date, user_id, habit_id
  UNIQUE (habit_id, user_id)   -- a user can only like a given habit once
  FK habit_id -> habits.id ON DELETE CASCADE
```

No changes to the `notifications` / `notifications_users` tables —
`HABIT_LIKE` is just another `NotificationType` value using the same generic
entity, exactly like `EVENT_LIKE`.

---

## What stayed backend-agnostic on purpose

Same as every other notification feature in this codebase: date/time
formatting ("Today" / "Yesterday" / `DD.MM.YYYY HH:MM XM`) is left to the
frontend — `NotificationDto.time` is a raw `ZonedDateTime`.

---

## API surface (`HabitLikeController`)

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/habit/likes/{habitId}` | USER/ADMIN/MODERATOR/UBS_EMPLOYEE | Like a habit (idempotent — already-liked is a no-op, still returns current state) |
| DELETE | `/habit/likes/{habitId}` | USER/ADMIN/MODERATOR/UBS_EMPLOYEE | Unlike a habit (idempotent — not-liked is a no-op) |

Both return `HabitLikeDtoResponse { habitId, likesCount, liked }`.
Notification retrieval/view/unread/delete/count reuse `NotificationController`
unchanged; `HABIT_LIKE` is just another value accepted by the
`notification-types` filter.

---

## Test coverage added

| Test class | Module | Covers |
|---|---|---|
| `HabitLikeServiceImplTest` (9 tests) | service | like/unlike happy paths, self-like guard, no-owner default-habit guard, already-liked idempotency, not-found paths |
| `HabitLikeNotificationListenerTest` (3) | service | like → `createNotification`, unlike → `removeActionUser`, self-like is ignored entirely |
| `HabitLikeControllerTest` (2) | core | both REST endpoints, response codes |

All new tests pass locally, alongside #105's own event-like tests
(unaffected — this branch only adds new files/enum values). The same two
pre-existing unrelated failures already called out on #104/#105/#110
(`UserVOMapperTest`, `LanguageRepoTest` — needs Docker/Testcontainers not
available in this environment) remain and are unrelated to this change. The
migration was validated statically (well-formed XML, unique changeset ids,
correct FK/unique-constraint ordering relative to `habits`) but not exercised
against a live database, for the same reason.
