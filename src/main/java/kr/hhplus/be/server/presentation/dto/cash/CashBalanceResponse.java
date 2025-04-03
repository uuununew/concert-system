package kr.hhplus.be.server.presentation.dto.cash;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "캐시 잔액 응답 DTO")
public record CashBalanceResponse(

            @Schema(description = "유저 ID", example = "42")
            Long userId,

            @Schema(description = "보유 캐시 금액", example = "30000")
            int cash

    ) {}

