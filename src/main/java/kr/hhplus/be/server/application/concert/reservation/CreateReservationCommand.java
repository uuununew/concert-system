package kr.hhplus.be.server.application.concert.reservation;

import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;

import java.math.BigDecimal;

public record CreateReservationCommand(
        Long userId,
        Long concertSeatId,
        BigDecimal price
) {
    public CreateReservationCommand {
        if (userId == null || userId <= 0) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "userId는 0보다 커야 합니다.");
        }
        if (concertSeatId == null || concertSeatId <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "reservationId는 0보다 커야 합니다.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "결제 금액은 0보다 커야 합니다.");
        }
    }
}
