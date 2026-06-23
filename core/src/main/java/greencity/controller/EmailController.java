package greencity.controller;

import greencity.constant.HttpStatuses;
import greencity.dto.email.ChangePlaceStatusDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {
    @Operation(summary = "Change place status via email.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    })
    @PostMapping("/changePlaceStatus")
    public ResponseEntity<ChangePlaceStatusDto> changePlaceStatus(
        @RequestBody @Valid ChangePlaceStatusDto changePlaceStatusDto) {
        return ResponseEntity.ok(changePlaceStatusDto);
    }
}
