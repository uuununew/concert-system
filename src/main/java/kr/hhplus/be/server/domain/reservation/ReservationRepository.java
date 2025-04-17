package kr.hhplus.be.server.domain.reservation;

import kr.hhplus.be.server.domain.concert.ConcertSeat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository{
    Optional<Reservation> findById(Long id);
    Reservation save(Reservation reservation);
    List<Reservation> findAllByStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime cutoffTime);
    Optional<Reservation> findByConcertSeatAndStatus(ConcertSeat seat, ReservationStatus status);
}
