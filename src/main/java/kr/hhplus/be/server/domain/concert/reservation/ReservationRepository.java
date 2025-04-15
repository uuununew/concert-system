package kr.hhplus.be.server.domain.concert.reservation;

import kr.hhplus.be.server.domain.concert.ConcertSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime cutoffTime);
    Optional<Reservation> findByConcertSeatAndStatus(ConcertSeat seat, ReservationStatus status);
}
