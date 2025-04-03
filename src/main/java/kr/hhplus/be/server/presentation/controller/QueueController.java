package kr.hhplus.be.server.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.common.ErrorResponse;
import kr.hhplus.be.server.presentation.dto.queue.QueueTokenRequest;
import kr.hhplus.be.server.presentation.dto.queue.QueueTokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/queue")
@Tag(name = "Queue", description = "대기열 관련 API")
public class QueueController {
    @Operation(
            summary = "대기열 토큰 발급",
            description = "예약을 위한 대기열에 진입할 수 있는 임시 토큰을 발급합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "토큰 발급 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = QueueTokenResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @PostMapping("/token")
    public ResponseEntity<QueueTokenResponse> issueToken(@RequestBody QueueTokenRequest request) {
        String token = UUID.randomUUID().toString();
        QueueTokenResponse response = new QueueTokenResponse(token, LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
