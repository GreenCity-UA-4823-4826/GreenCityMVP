package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.habitinvite.HabitInviteDto;
import greencity.dto.user.UserVO;
import org.springframework.data.domain.Pageable;

public interface HabitInviteService {
    /**
     * Method to invite a user to add a habit. Idempotent for a repeated pending
     * invite from the same inviter to the same invitee for the same habit - it just
     * refreshes the existing invite and notification instead of creating a
     * duplicate. Publishes a notification event for the invitee.
     *
     * @param habitId   id of the habit the invitee is invited to add.
     * @param inviteeId id of the user being invited.
     * @param inviter   {@link UserVO} that sends the invite.
     * @return {@link HabitInviteDto} instance.
     */
    HabitInviteDto sendInvite(Long habitId, Long inviteeId, UserVO inviter);

    /**
     * Method for the inviter to cancel a previously sent, still-pending invite.
     * Retracts the invitee's pending notification for this habit.
     *
     * @param inviteId id of the invite to cancel.
     * @param user     current {@link UserVO} - must be the invite's inviter.
     * @return {@link HabitInviteDto} instance.
     */
    HabitInviteDto cancelInvite(Long inviteId, UserVO user);

    /**
     * Method for the invitee to accept a pending invite. Retracts the invitee's
     * pending notification for this habit.
     *
     * @param inviteId id of the invite to accept.
     * @param user     current {@link UserVO} - must be the invite's invitee.
     * @return {@link HabitInviteDto} instance.
     */
    HabitInviteDto acceptInvite(Long inviteId, UserVO user);

    /**
     * Method for the invitee to decline a pending invite. Retracts the invitee's
     * pending notification for this habit.
     *
     * @param inviteId id of the invite to decline.
     * @param user     current {@link UserVO} - must be the invite's invitee.
     * @return {@link HabitInviteDto} instance.
     */
    HabitInviteDto declineInvite(Long inviteId, UserVO user);

    /**
     * Method returns the current user's own pending sent invites, so they can
     * choose which one to cancel.
     *
     * @param inviter  current {@link UserVO}.
     * @param pageable pageable configuration.
     * @return page of the inviter's own pending invites.
     */
    PageableDto<HabitInviteDto> getSentPendingInvites(UserVO inviter, Pageable pageable);
}
