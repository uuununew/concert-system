package kr.hhplus.be.server.domain.reservation;

import java.time.LocalDateTime;

public interface ReservationInfoSender {
    void sendReservationInfo(Long reservationId, LocalDateTime reservedAt);
}
