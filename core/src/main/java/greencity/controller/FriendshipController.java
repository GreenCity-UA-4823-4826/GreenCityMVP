package greencity.controller;

import greencity.annotations.CurrentUser;
import greencity.constant.HttpStatuses;
import greencity.dto.user.UserFriendDto;
import greencity.dto.user.UserVO;
import greencity.service.FriendshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/friends")
public class FriendshipController {
    private final FriendshipService friendshipService;

    /**
     * Method for getting the count of friends for the current authorized user.
     *
     * @param userVO - current authorized user.
     * @return {@link ResponseEntity} with number of friends as {@code long}.
     */
    @Operation(summary = "Get count of current user's friends")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    })
    @GetMapping("/count")
    public ResponseEntity<Long> getFriendsCount(
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity.ok(friendshipService.countOfUserFriends(userVO));
    }

    /**
     * Method for searching users by name.
     *
     * @param userVO   - current authorized user.
     * @param query    - search string to filter by name.
     * @param pageable - pagination parameters.
     * @return {@link ResponseEntity} with {@link Page} of {@link UserFriendDto}.
     */
    @Operation(summary = "Search users by name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    })
    @GetMapping("/search")
    public ResponseEntity<Page<UserFriendDto>> searchFriends(
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @RequestParam @Size(min = 1, max = 30)  String query,
        Pageable pageable) {
        return ResponseEntity.ok(friendshipService.searchFriends(userVO, query, pageable));
    }

    /**
     * Method for sending a friend request to another user.
     *
     * @param userVO     - current authorized user.
     * @param receiverId - id of the user to send request to.
     * @return {@link ResponseEntity} with status 201.
     */
    @Operation(summary = "Send a friend request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/{receiverId}")
    public ResponseEntity<Void> addFriend(
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @PathVariable Long receiverId) {
        friendshipService.addFriend(userVO, receiverId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Method for cancelling a friend request sent by current user.
     *
     * @param userVO     - current authorized user.
     * @param receiverId - id of the user to cancel request for.
     * @return {@link ResponseEntity} with status 200.
     */
    @Operation(summary = "Cancel a friend request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/{receiverId}")
    public ResponseEntity<Void> cancelFriendRequest(
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @PathVariable Long receiverId) {
        friendshipService.cancelFriendRequest(userVO, receiverId);
        return ResponseEntity.ok().build();
    }
}