package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;

import java.math.BigDecimal;

public record CreatePaymentCommand(
        Long userId,
        Long reservationId,
        BigDecimal amount
) {
    public CreatePaymentCommand {
        if (userId == null || userId <= 0) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "userId는 필수이며 0보다 커야 합니다.");
        }
        if (reservationId == null || reservationId <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "concertSeatId는 필수이며 0보다 커야 합니다.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "price는 필수이며 0보다 커야 합니다.");
        }
    }
}
