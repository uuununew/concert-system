package kr.hhplus.be.server.domain.reservation;

import java.time.LocalDateTime;

public interface ReservationEventPublisher {
    void publishReservationCompleted(Long reservationId, LocalDateTime reservedAt);
}
