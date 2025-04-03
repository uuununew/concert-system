package kr.hhplus.be.server.presentation.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 요청 DTO")
public class PaymentRequest {
    @Schema(description = "유저 ID", example = "42", required = true)
    private Long userId;

    @Schema(description = "예약 ID", example = "9001", required = true)
    private Long reservationId;

    @Schema(description = "결제 금액", example = "10000", required = true)
    private int amount;

    public PaymentRequest() {}

    public PaymentRequest(Long userId, Long reservationId, int amount) {
        this.userId = userId;
        this.reservationId = reservationId;
        this.amount = amount;
    }

    public Long getUserId() { return userId; }
    public Long getReservationId() { return reservationId; }
    public int getAmount() { return amount; }
}
