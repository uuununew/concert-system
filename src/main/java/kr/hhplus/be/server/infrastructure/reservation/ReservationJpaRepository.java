package kr.hhplus.be.server.infrastructure.reservation;

import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime cutoffTime);
    Optional<Reservation> findByConcertSeatAndStatus(ConcertSeat seat, ReservationStatus status);
}

