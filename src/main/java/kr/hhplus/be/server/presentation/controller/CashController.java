package kr.hhplus.be.server.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.common.ErrorResponse;
import kr.hhplus.be.server.presentation.dto.cash.CashBalanceResponse;
import kr.hhplus.be.server.presentation.dto.cash.CashChargeRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cash")
@Tag(name = "Cash", description = "캐시(잔액) 관련 API")
public class CashController {
    @Operation(
            summary = "캐시 잔액 조회",
            description = "유저 ID를 기반으로 현재 보유한 캐시 금액을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CashBalanceResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "유저 없음",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @GetMapping
    public ResponseEntity<CashBalanceResponse> getCash(@RequestParam("userId") Long userId) {
        CashBalanceResponse response = new CashBalanceResponse(userId, 30000);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "캐시 충전",
            description = "요청된 금액만큼 유저의 캐시를 충전합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "충전 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CashBalanceResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "유저 없음",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @PostMapping("/charge")
    public ResponseEntity<CashBalanceResponse> chargeCash(@RequestBody CashChargeRequest request) {
        CashBalanceResponse response = new CashBalanceResponse(request.getUserId(), 30000 + request.getAmount());
        return ResponseEntity.ok(response);
    }
}
