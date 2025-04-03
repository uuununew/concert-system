package kr.hhplus.be.server.presentation.dto.cash;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "캐시 충전 요청 DTO")
public class CashChargeRequest {

    @Schema(description = "유저 ID", example = "42", required = true)
    private Long userId;

    @Schema(description = "충전할 금액", example = "10000", required = true)
    private int amount;

    public CashChargeRequest() {}

    public CashChargeRequest(Long userId, int amount) {
        this.userId = userId;
        this.amount = amount;
    }

    public Long getUserId() {
        return userId;
    }

    public int getAmount() {
        return amount;
    }
}
