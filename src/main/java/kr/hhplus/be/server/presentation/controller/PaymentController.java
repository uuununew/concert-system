package kr.hhplus.be.server.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.common.ErrorResponse;
import kr.hhplus.be.server.presentation.dto.payment.PaymentRequest;
import kr.hhplus.be.server.presentation.dto.payment.PaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payment", description = "결제 관련 API")
public class PaymentController {
    @Operation(
            summary = "결제 요청",
            description = "유저가 예약한 좌석에 대해 보유 캐시로 결제합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "결제 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PaymentResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잔액 부족 또는 유효하지 않은 요청",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "예약 내역 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "결제 처리 중 서버 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @PostMapping
    public ResponseEntity<PaymentResponse> pay(@RequestBody PaymentRequest request) {
        PaymentResponse response = new PaymentResponse(5001L, LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
