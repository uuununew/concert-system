package kr.hhplus.be.server.infrastructure.concert.reservation;

import kr.hhplus.be.server.domain.concert.reservation.Reservation;
import kr.hhplus.be.server.domain.concert.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByConcertSeatIdAndStatus(Long concertSeatId, ReservationStatus status);

    List<Reservation> findAllByStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime dateTime);
}
