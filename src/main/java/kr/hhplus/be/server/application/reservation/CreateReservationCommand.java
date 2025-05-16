package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;

import java.math.BigDecimal;

public record CreateReservationCommand(
        String tokenId,
        Long userId,
        Long concertSeatId,
        BigDecimal price
) {
    public CreateReservationCommand {
        if (tokenId == null || tokenId.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "tokenId는 필수입니다.");
        }
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
