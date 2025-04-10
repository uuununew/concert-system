package kr.hhplus.be.server.presentation.concert.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.application.concert.payment.CreatePaymentCommand;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotNull(message = "예약 ID는 필수입니다.")
        Long reservationId,

        @NotNull(message = "결제 금액은 필수입니다.")
        @DecimalMin(value = "1", inclusive = false, message = "결제 금액은 0보다 커야 합니다.")
        BigDecimal amount
) {
    public CreatePaymentCommand toCommand(Long userId) {
        return new CreatePaymentCommand(userId, reservationId, amount);
    }
}
