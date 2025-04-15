package kr.hhplus.be.server.domain.concert;

import kr.hhplus.be.server.domain.concert.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConcertSeatRepository{

    List<ConcertSeat> findAllByConcert_Id(Long concertId);

    List<ConcertSeat> findAllByConcert_IdAndStatus(Long concertId, SeatStatus status);

    ConcertSeat save(ConcertSeat seat);

    Optional<ConcertSeat> findById(Long id);

    boolean existsByConcert_Id(Long concertId);
}