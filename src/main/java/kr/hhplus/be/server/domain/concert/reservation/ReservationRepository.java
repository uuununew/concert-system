package kr.hhplus.be.server.domain.concert.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime cutoffTime);
    Optional<Reservation> findByConcertSeatIdAndStatus(Long concertSeatId, ReservationStatus status);
}
