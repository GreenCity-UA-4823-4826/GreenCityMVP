package greencity.controller;

import greencity.annotations.ApiPageableWithoutSort;
import greencity.annotations.CurrentUser;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableDto;
import greencity.dto.habitcomment.AddHabitCommentDtoRequest;
import greencity.dto.habitcomment.AddHabitCommentDtoResponse;
import greencity.dto.habitcomment.HabitCommentDto;
import greencity.dto.user.UserVO;
import greencity.service.HabitCommentService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/habit/comments")
public class HabitCommentController {
    private final HabitCommentService habitCommentService;

    @Operation(summary = "Add comment to habit.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = AddHabitCommentDtoResponse.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/{habitId}")
    public ResponseEntity<AddHabitCommentDtoResponse> save(@PathVariable Long habitId,
        @Valid @RequestBody AddHabitCommentDtoRequest request,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(habitCommentService.save(habitId, request, user));
    }

    @Operation(summary = "Get all comments to habit.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/{habitId}")
    @ApiPageableWithoutSort
    public ResponseEntity<PageableDto<HabitCommentDto>> findAllComments(
        @Parameter(hidden = true) Pageable pageable,
        @PathVariable Long habitId) {
        return ResponseEntity.ok(habitCommentService.findAllComments(pageable, habitId));
    }

    @Operation(summary = "Delete habit comment.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/{habitCommentId}")
    public ResponseEntity<Object> delete(@PathVariable Long habitCommentId,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        habitCommentService.deleteById(habitCommentId, user);
        return ResponseEntity.ok().build();
    }
}
