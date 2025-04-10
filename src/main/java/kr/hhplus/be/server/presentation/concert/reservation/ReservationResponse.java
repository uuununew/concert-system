package kr.hhplus.be.server.presentation.concert.reservation;

import kr.hhplus.be.server.domain.concert.reservation.Reservation;
import kr.hhplus.be.server.domain.concert.reservation.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long userId,
        Long concertSeatId,
        BigDecimal price,
        ReservationStatus status,
        LocalDateTime paidAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getConcertSeatId(),
                reservation.getPrice(),
                reservation.getStatus(),
                reservation.getPaidAt(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}
