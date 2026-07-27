# Notification on Habit Comment

Feature branch: `feature/110-habit-was-commented-notification` (based on `feature/104-event-was-commented-notification`)
GitHub issue: none found in this repo's issue tracker — tracked externally as ticket #110.

## User story

> As a habit owner I want to be notified when someone comments on my habit so that I can reply soon.

**Preconditions:** a user creates a custom habit, another user leaves a comment on it. Habits had no comment feature at all before this branch — it's built from scratch here, alongside the notification.

**Acceptance criteria:**
- Leaving a comment on someone else's habit creates a notification for the habit's owner. Commenting on your own habit never notifies you (self-comment guard).
- Default/global habits (not custom-created by any single user) have no owner at all — commenting on them never triggers a notification, since there's no one to notify. See "Habit ownership" below.
- If several *different* users comment on the same habit before the owner views the notification, the notification is aggregated: last two commenters + "and other users" for 3+, same as event/eco-news comments.
- If the same user comments more than once on the same habit before viewing, the record is updated with only the latest comment's data (no duplicate entries) — same dedup-by-actor logic already used everywhere else.
- The unread counter increases by one **per newly created notification row** (not per comment) — i.e. row-based counting, matching what #104 already does. This is a deliberate difference from `feature/105-event-was-liked-notification`, which changed the shared counter to be actor-based; this branch does not build on #105 and keeps the original row-based semantics, per this ticket's own spec.
- Only the owner can view/mark/delete their own comment notifications (inherited unchanged from #104's ownership checks).

---

## Habit ownership: why this needed a design decision

Unlike `Event.organizer` (`@ManyToOne User`) or `EcoNews.author` (`@ManyToOne User`), **`Habit` has no proper JPA relation to a `User`** — only a raw `@Column(name = "user_id") private Long userId;` (`dao/src/main/java/greencity/entity/Habit.java:37-38`), populated **only when a custom habit is created** (`HabitServiceImpl.addCustomHabit`). Default/global habits leave it unset — they have no single owner by design.

Two options were considered:
1. Treat "notify owner" as a no-op for default habits (`userId == null`), since there genuinely is no single person to notify — **chosen**.
2. Promote `userId` to a real `@ManyToOne User` relation on `Habit` — rejected as unnecessarily invasive for this feature; it doesn't resolve the actual question of "who owns a default habit," it just changes how the (still-absent) owner would be modeled.

`HabitCommentServiceImpl.save()` implements option 1: it reads `habit.getUserId()` directly (no lazy-proxy re-fetch needed, unlike `Event.organizer` — it's a plain `Long`), and skips publishing the notification entirely when it's `null`, or when it equals the commenter's own id.

---

## Architecture overview

```
HabitCommentServiceImpl.save(habitId, request, commenter)
        │  (habit.userId != null && commenter != owner)
        ▼
ApplicationEventPublisher.publishEvent(HabitCommentNotificationEvent)
        │  (decoupled, in-process Spring event)
        ▼
HabitCommentNotificationListener.onHabitComment()   [@Async, @TransactionalEventListener(AFTER_COMMIT)]
        │  (skips if commenter == owner, defense in depth)
        ▼
UserNotificationService.createNotification(owner, commenter, HABIT_COMMENT, habitId, habitName)
        │
        ├─ find existing UNVIEWED notification for (owner, HABIT_COMMENT, habitId)
        │      found  → merge commenter into it, refresh time/habitName
        │      absent → build a brand-new Notification, unread counter +1
        ▼
NotificationRepo.save(...)
        ▼
SimpMessagingTemplate.convertAndSend("/topic/{userId}/notification", unreadCount)
```

This reuses the exact same generic `UserNotificationService`/`Notification`/
`NotificationRepo` merge-and-count logic already documented in
[`docs/econews-comment-notification.md`](econews-comment-notification.md) and
[`docs/event-comment-notification.md`](event-comment-notification.md) —
**unchanged** by this branch. `HABIT_COMMENT` is simply a third
`NotificationType` value riding the same infrastructure.

### Resolving the habit's display name

`Habit` has no plain `title`/`name` field (unlike `Event.title` or
`EcoNews.title`) — names are localized via `HabitTranslation` (one row per
`(habit, language)`). The notification's `secondMessage` (the "habit name"
shown in the templated text) is resolved via
`HabitTranslationRepo.findByHabitAndLanguageCode(habit, AppConstant.DEFAULT_LANGUAGE_CODE)`
(`"en"`), falling back to `null` if no English translation exists for that
habit. This is a simplification: the notification text is always built in the
habit's English name regardless of the *commenter's* or *owner's* locale —
acceptable for now since every seeded habit has an English translation, but a
future iteration could resolve the name in the recipient's own language
instead.

### Key files

| Layer | File | Responsibility |
|---|---|---|
| Entity | `dao/.../entity/HabitComment.java` | Comment text, author, habit, `@CreatedDate` |
| Repo | `dao/.../repository/HabitCommentRepo.java` | Paged fetch by habit (newest first), `existsByIdAndUserId` for delete-ownership |
| Service | `service/.../service/HabitCommentServiceImpl.java` | Save/list/delete; publishes the notification event only when the habit has an owner who isn't the commenter |
| Event | `service-api/.../event/HabitCommentNotificationEvent.java` | Immutable payload: owner, commenter, habitId, habitName |
| Listener | `service/.../service/HabitCommentNotificationListener.java` | Async, after-commit consumer; skips self-comments; delegates to `UserNotificationService` |
| Controller | `core/.../controller/HabitCommentController.java` | `POST`/`GET /habit/comments/{habitId}`, `DELETE /habit/comments/{habitCommentId}` |
| DTOs | `service-api/.../dto/habitcomment/{AddHabitCommentDtoRequest,AddHabitCommentDtoResponse,HabitCommentAuthorDto,HabitCommentDto}.java` | API contract |
| Mapper | `service/.../mapping/HabitCommentDtoMapper.java` | `HabitComment` entity → `HabitCommentDto` |
| Migration | `dao/.../db/changelog/logs/ch-add-table-habit-comment.xml` | `habit_comment` table, FKs to `users`/`habits` |
| i18n | `core/src/main/resources/notification{,_uk}.properties` | `HABIT_COMMENT` / `HABIT_COMMENT_TITLE` templates |

---

## Data model

```
habit_comment
  id, text, created_date, user_id, habit_id
  FK habit_id -> habits.id ON DELETE CASCADE
  FK user_id  -> users.id
```

No changes to `notifications` / `notifications_users` — `HABIT_COMMENT` is
just another `NotificationType` value; `target_id` is the habit id and
`second_message` is the habit's English name (see above).

---

## Why this branch is off #104, not #105

`feature/105-event-was-liked-notification` changed the shared unread-count
query from "count unread `Notification` rows" to "sum distinct `actionUsers`
across unread notifications," because that ticket's spec explicitly required
the counter to increase per individual like, even when aggregated.

This ticket's spec says the opposite: *"increment the counter by one when a
new notification is created"* — i.e. row-based counting, only on the
`buildNotification` branch, not on every merge into an existing unread row.
That's exactly what #104 already does and #105 later changed. Branching from
#105 would have forced a choice between violating this ticket's spec or
reverting #105's (also spec-driven) change. Branching from #104 sidesteps the
conflict entirely — this feature needs nothing #105 added (no unlike/removal
semantics apply to comments) and keeps the counter behavior this ticket
actually asks for.

---

## What stayed backend-agnostic on purpose

Same as every other comment-notification feature in this codebase: date/time
formatting ("Today" / "Yesterday" / `DD.MM.YYYY HH:MM XM`) is left to the
frontend — `NotificationDto.time` is a raw `ZonedDateTime`. "Habit name as a
link" only needs `targetId` + `secondMessage` from the DTO.

---

## API surface

### `HabitCommentController`

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/habit/comments/{habitId}` | USER/ADMIN/MODERATOR/UBS_EMPLOYEE | Add a comment, publishes the notification event (if the habit has an owner) |
| GET | `/habit/comments/{habitId}` | USER/ADMIN/MODERATOR/UBS_EMPLOYEE | Paged list of comments, newest first |
| DELETE | `/habit/comments/{habitCommentId}` | author or ADMIN | Delete a comment |

`NotificationController` is unchanged — see `docs/econews-comment-notification.md`
and `docs/event-comment-notification.md` for its full endpoint table; `HABIT_COMMENT`
is just another value accepted by the `notification-types` filter.

---

## Test coverage added

| Test class | Module | Covers |
|---|---|---|
| `HabitCommentServiceImplTest` (10 tests) | service | save publishes/skips notification (self-comment guard, no-owner default-habit guard), not-found paths, list/delete (author vs. admin vs. neither) |
| `HabitCommentNotificationListenerTest` (2) | service | event → `createNotification` delegation; self-comment is ignored |
| `HabitCommentControllerTest` (3) | core | all 3 REST endpoints, response codes |

All new tests pass locally; the same two pre-existing unrelated failures
already called out on #104/#105 (`UserVOMapperTest`, and `LanguageRepoTest`
which needs Docker/Testcontainers not available in this environment) are
unaffected by this branch. The migration was validated statically (well-formed
XML, unique changeset ids, correct FK ordering relative to `habits`) but not
exercised against a live database, for the same reason.
