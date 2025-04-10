package kr.hhplus.be.server.presentation.concert.payment;

import kr.hhplus.be.server.domain.concert.payment.Payment;

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
