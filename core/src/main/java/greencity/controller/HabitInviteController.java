package greencity.controller;

import greencity.annotations.ApiPageableWithoutSort;
import greencity.annotations.CurrentUser;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableDto;
import greencity.dto.habitinvite.HabitInviteDto;
import greencity.dto.habitinvite.SendHabitInviteDtoRequest;
import greencity.dto.user.UserVO;
import greencity.service.HabitInviteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/habit/invites")
public class HabitInviteController {
    private final HabitInviteService habitInviteService;

    @Operation(summary = "Invite a user to add a habit.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = HabitInviteDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/{habitId}")
    public ResponseEntity<HabitInviteDto> sendInvite(@PathVariable Long habitId,
        @Valid @RequestBody SendHabitInviteDtoRequest request,
        @Parameter(hidden = true) @CurrentUser UserVO inviter) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(habitInviteService.sendInvite(habitId, request.getInviteeId(), inviter));
    }

    @Operation(summary = "Cancel a pending habit invite (by the inviter).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/{inviteId}/cancel")
    public ResponseEntity<HabitInviteDto> cancelInvite(@PathVariable Long inviteId,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        return ResponseEntity.ok(habitInviteService.cancelInvite(inviteId, user));
    }

    @Operation(summary = "Accept a pending habit invite (by the invitee).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/{inviteId}/accept")
    public ResponseEntity<HabitInviteDto> acceptInvite(@PathVariable Long inviteId,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        return ResponseEntity.ok(habitInviteService.acceptInvite(inviteId, user));
    }

    @Operation(summary = "Decline a pending habit invite (by the invitee).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/{inviteId}/decline")
    public ResponseEntity<HabitInviteDto> declineInvite(@PathVariable Long inviteId,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        return ResponseEntity.ok(habitInviteService.declineInvite(inviteId, user));
    }

    @Operation(summary = "Get current user's own pending sent habit invites.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    })
    @GetMapping("/sent")
    @ApiPageableWithoutSort
    public ResponseEntity<PageableDto<HabitInviteDto>> getSentPendingInvites(
        @Parameter(hidden = true) Pageable pageable,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        return ResponseEntity.ok(habitInviteService.getSentPendingInvites(user, pageable));
    }
}
