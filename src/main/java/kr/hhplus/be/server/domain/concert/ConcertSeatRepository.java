package kr.hhplus.be.server.domain.concert;

import kr.hhplus.be.server.domain.concert.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConcertSeatRepository extends JpaRepository<ConcertSeat, Long> {
    List<ConcertSeat> findAllByConcertId(Long concertId);

    List<ConcertSeat> findAllByConcertIdAndStatus(Long concertId, SeatStatus status);
}