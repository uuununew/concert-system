package kr.hhplus.be.server.domain.reservation;

import java.time.LocalDateTime;

public record ReservationCompletedEvent(
        Long reservationId,
        LocalDateTime reservedAt
) {
}