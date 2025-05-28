package kr.hhplus.be.server.domain.reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationCompletedEvent(
        Long reservationId,
        Long userId,
        BigDecimal price,
        LocalDateTime reservedAt
) {
}