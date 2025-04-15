package kr.hhplus.be.server.presentation.payment;

import kr.hhplus.be.server.domain.payment.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long userId,
        Long reservationId,
        BigDecimal amount,
        LocalDateTime paidAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getUserId(),
                payment.getReservationId(),
                payment.getAmount(),
                payment.getPaidAt()
        );
    }
}
