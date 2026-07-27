# Notification on Habit Invitation

Feature branch: `feature/112-habit-invitation-notification` (based on `feature/111-habit-was-liked-notification`)
GitHub issue: none found in this repo's issue tracker — tracked externally as ticket #112, which also folds in an additional sub-task ("Скасування запрошення відправником" — cancellation of a sent invite by its sender) covered in the same branch.

## User story

> As a user (User1) I want to invite another user (User2) to add a habit, and be notified... actually the notification goes the other way: **User2** (the invitee) is notified that User1 invited them.

**Preconditions:** User1 sends an invite to User2 for a specific habit. Habit invites had no prior implementation — entity, status lifecycle, and notification are all new.

**Acceptance criteria:**
- Sending an invite creates/updates a notification for the **invitee** (User2), not the inviter.
- A user cannot invite themselves (`inviteeId == inviter.id` → `400 BadRequestException`).
- A user cannot be invited to a habit they've already added themselves (checked via `HabitAssignRepo.findByHabitIdAndUserId` → `400 BadRequestException` if present).
- If several *different* users invite the same invitee to the same habit before the invitee views the notification, it's aggregated: last two inviters + "and other users" for 3+.
- If the same inviter invites the same invitee to several *different* habits before viewing, each habit gets its **own** notification row (no cross-habit aggregation), newest first.
- Re-inviting the same invitee to the same habit while a prior invite from the same inviter is still `PENDING` is **idempotent** — it refreshes the existing invite's `updatedDate` and bumps the notification, rather than creating a duplicate invite row.
- The unread counter increases by exactly one per real invite (even when aggregated) and decreases back when an invite leaves `PENDING` status (cancelled/accepted/declined) — the same **actor-based** counter from #105/#111, not the row-based one from #110.
- Only the invitee can view/mark/delete their own invite notifications.

**Additional sub-task folded into this branch — cancellation by the sender:**
- `POST /habit/invites/{inviteId}/cancel` lets the **inviter** cancel their own still-`PENDING` invite.
- Only the original inviter can cancel (`403` otherwise); the invite must still be `PENDING` (`400` otherwise — see the note on HTTP status conventions below).
- The invite row is marked `CANCELLED`, not deleted, preserving history/audit trail.
- If the invitee hasn't viewed the notification yet, cancelling retracts just that inviter's contribution from the (possibly aggregated) notification — other pending inviters for the same habit are untouched. The unread counter decrements accordingly.

---

## Design decisions made explicit before implementation

This feature required more upfront decisions than the comment/like features, since an "invite" has a real lifecycle instead of being a simple toggle. All of the following were confirmed before writing code:

1. **Base branch: #111, not #110.** This ticket's counter spec ("increment per new invite, even aggregated") is the actor-based semantics from #105/#111, not the row-based semantics #110 deliberately kept. Branching from #111 gets `removeActionUser` and `countUnreadActionUsersByTargetUserId` for free.
2. **Repeated pending invite → idempotent refresh, not a new row and not a 400.** Mirrors the "like" idempotency philosophy (`existsBy...` guards a duplicate action) rather than treating a repeat invite as an error.
3. **Blocking invites once the invitee already has the habit assigned** (`HabitAssignRepo.findByHabitIdAndUserId(...).isPresent()`) — an invite to add something you already have is meaningless, so it's rejected outright rather than silently allowed.
4. **Explicit `accept`/`decline` endpoints were added**, even though the original spec listed them as optional, because the *companion* cancellation sub-task's edge case ("if the invite was already accepted, cancelling no longer makes sense") is otherwise undefined — there'd be no reliable way to know an invite was "accepted" without a real status transition to check. `HabitInviteStatus` is `PENDING → ACCEPTED | DECLINED | CANCELLED`, a superset of `FriendshipStatus` (`PENDING/ACCEPTED/REJECTED`, see `service-api/src/main/java/greencity/enums/friendship/FriendshipStatus.java`) since this feature needs to distinguish *who* ended the pending state (invitee declining vs. inviter cancelling), which friendship's simpler 3-value enum doesn't need to.
5. **No 409 Conflict.** The original spec sketch suggested 409 for "already handled" cases. This codebase's `CustomExceptionHandler` has **no 409 mapping anywhere** — every "already exists / already done" case across all 39+ existing exception classes flattens to `400 BAD_REQUEST` (e.g. `UserAlreadyHasHabitAssignedException`, `NotSavedException`, `WrongIdException`). Introducing the first-ever 409 here would be inconsistent with the rest of the codebase, so "invite already handled" and "invitee already has the habit" both throw `BadRequestException` → `400`, matching every sibling case.
6. **"Accepted" is a status flag on the invite itself, not inferred from `HabitAssign`.** Accepting an invite does **not** automatically create a `HabitAssign` row — that remains the job of the existing `/habit/assign` endpoint. This keeps the invite lifecycle and the actual habit-assignment mechanism decoupled; a future iteration could wire them together, but this branch does not, to avoid scope creep into `HabitAssignServiceImpl`.

---

## Architecture overview

```text
HabitInviteServiceImpl.sendInvite(habitId, inviteeId, inviter)
        │  (not self-invite, invitee doesn't already have the habit)
        ▼
find existing PENDING invite for (habit, inviter, invitee)
        │      found  → bump updatedDate, reuse row
        │      absent → create new PENDING HabitInvite row
        ▼
ApplicationEventPublisher.publishEvent(HabitInviteNotificationEvent{ active: true })
        ▼
HabitInviteNotificationListener.onHabitInvite()   [@Async, @TransactionalEventListener(AFTER_COMMIT)]
        ▼
UserNotificationService.createNotification(invitee, inviter, HABIT_INVITE, habitId, habitName)


HabitInviteServiceImpl.cancelInvite / acceptInvite / declineInvite (inviteId, actingUser)
        │  (ownership check: inviter for cancel, invitee for accept/decline)
        │  (invite.status must still be PENDING, else 400)
        ▼
invite.status = CANCELLED | ACCEPTED | DECLINED ; save
        ▼
ApplicationEventPublisher.publishEvent(HabitInviteNotificationEvent{ active: false })
        ▼
HabitInviteNotificationListener.onHabitInvite()
        ▼
UserNotificationService.removeActionUser(invitee, inviter, HABIT_INVITE, habitId)
        │
        ├─ no unread notification for this habit        → no-op
        ├─ unread notification, other inviters remain    → this inviter removed, notification saved
        └─ unread notification, this was the only inviter → notification deleted
```

`createNotification` and `removeActionUser` are both fully generic and unchanged — see
[`docs/event-like-notification.md`](event-like-notification.md) (where `removeActionUser`
was introduced) and [`docs/econews-comment-notification.md`](econews-comment-notification.md)
(aggregation mechanics). This feature is the first to trigger `removeActionUser` from three
different transitions (cancel, accept, decline) instead of just one ("unlike"), which is
exactly why it was designed as a generic "retract this actor" primitive rather than an
unlike-specific method.

### Why cancel/accept/decline all call the *same* retraction path

Regardless of *who* ends the pending state or *why*, the invitee's pending notification about
that specific inviter is no longer actionable once the invite leaves `PENDING` — there's
nothing left to accept or decline, and continuing to show it would be misleading. All three
transitions therefore retract that one inviter's contribution identically. If the invitee had
pending invites to the same habit from other inviters too, those are untouched — this is the
same aggregation-scoped removal semantics as unlike.

### Key files

| Layer | File | Responsibility |
|---|---|---|
| Enum | `service-api/.../enums/HabitInviteStatus.java` | `PENDING, ACCEPTED, DECLINED, CANCELLED` |
| Entity | `dao/.../entity/HabitInvite.java` | habit, inviter, invitee, status, createdDate, updatedDate |
| Repo | `dao/.../repository/HabitInviteRepo.java` | Find existing pending invite, ownership-check helpers, paged "sent" list |
| Service | `service/.../service/HabitInviteServiceImpl.java` | send/cancel/accept/decline, validation, notification publishing |
| Event | `service-api/.../event/HabitInviteNotificationEvent.java` | Immutable payload: invitee, inviter, habitId, habitName, `active` flag |
| Listener | `service/.../service/HabitInviteNotificationListener.java` | Async, after-commit consumer; routes to create vs. remove based on `active` |
| Controller | `core/.../controller/HabitInviteController.java` | `POST /habit/invites/{habitId}`, `POST /habit/invites/{inviteId}/{cancel,accept,decline}`, `GET /habit/invites/sent` |
| DTOs | `service-api/.../dto/habitinvite/{SendHabitInviteDtoRequest,HabitInviteDto}.java` | API contract |
| Migration | `dao/.../db/changelog/logs/ch-add-table-habit-invite.xml` | `habit_invite` table |
| i18n | `core/src/main/resources/notification{,_uk}.properties` | `HABIT_INVITE` / `HABIT_INVITE_TITLE` templates |
| Errors | `service-api/.../constant/ErrorMessage.java` | `CANNOT_INVITE_YOURSELF`, `HABIT_INVITE_NOT_FOUND_BY_ID`, `HABIT_INVITE_ALREADY_HANDLED`, `INVITEE_ALREADY_HAS_HABIT_ASSIGNED` |

No changes to `UserNotificationService`/`UserNotificationServiceImpl`, `NotificationController`,
`NotificationDto`, or the actor-based counting logic — all reused as-is from #105.

---

## Data model

```text
habit_invite
  id, habit_id, inviter_id, invitee_id, status, created_date, updated_date
  FK habit_id   -> habits.id ON DELETE CASCADE
  FK inviter_id -> users.id
  FK invitee_id -> users.id
```

No changes to the `notifications` / `notifications_users` tables — `HABIT_INVITE` is just
another `NotificationType` value using the same generic entity.

---

## API surface (`HabitInviteController`)

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/habit/invites/{habitId}` | USER/ADMIN/MODERATOR/UBS_EMPLOYEE | Send an invite (body: `{inviteeId}`). `201`, or `400` for self-invite/already-assigned |
| POST | `/habit/invites/{inviteId}/cancel` | USER/ADMIN/MODERATOR/UBS_EMPLOYEE | Inviter cancels their own pending invite. `403` if not the inviter, `400` if not `PENDING` |
| POST | `/habit/invites/{inviteId}/accept` | USER/ADMIN/MODERATOR/UBS_EMPLOYEE | Invitee accepts. `403` if not the invitee, `400` if not `PENDING` |
| POST | `/habit/invites/{inviteId}/decline` | USER/ADMIN/MODERATOR/UBS_EMPLOYEE | Invitee declines. `403` if not the invitee, `400` if not `PENDING` |
| GET | `/habit/invites/sent` | USER/ADMIN/MODERATOR/UBS_EMPLOYEE | Current user's own pending sent invites, paged — so they can pick which one to cancel |

All four action endpoints return `HabitInviteDto { id, habitId, habitName, inviterId, inviteeId, status, createdDate, updatedDate }`.

Notification retrieval/view/unread/delete/count reuse `NotificationController` unchanged;
`HABIT_INVITE` is just another value accepted by the `notification-types` filter.

---

## What stayed backend-agnostic on purpose

Same as every other notification feature in this codebase: date/time formatting
("Today" / "Yesterday" / `DD.MM.YYYY HH:MM XM`) is left to the frontend —
`NotificationDto.time` is a raw `ZonedDateTime`.

---

## Test coverage added

| Test class | Module | Covers |
|---|---|---|
| `HabitInviteServiceImplTest` (14 tests) | service | send (new invite, self-invite, habit/invitee not found, already-assigned, idempotent refresh), cancel (success, wrong user, not found, already handled), accept (success, wrong user), decline (success), sent-invites listing |
| `HabitInviteNotificationListenerTest` (2) | service | `active=true` → `createNotification`, `active=false` → `removeActionUser` |
| `HabitInviteControllerTest` (5) | core | all 5 REST endpoints, response codes |

All new tests pass locally, alongside #111's own habit-like tests and #105's event-like
tests (unaffected — this branch only adds new files/enum values, no changes to shared
notification code). The same pre-existing unrelated failures already called out on
#104/#105/#110/#111 (`UserVOMapperTest`, `LanguageRepoTest` — needs Docker/Testcontainers
not available in this environment) remain and are unrelated to this change. The migration
was validated statically (well-formed XML, unique changeset ids, correct FK ordering
relative to `habits`/`users`) but not exercised against a live database, for the same reason.
