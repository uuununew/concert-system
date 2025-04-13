package kr.hhplus.be.server.domain.concert;

import java.util.List;
import java.util.Optional;

public interface ConcertSeatRepository {
    List<ConcertSeat> findAllByConcertId(Long concertId);

    List<ConcertSeat> findAllByConcertIdAndStatus(Long concertId, SeatStatus status);

    ConcertSeat save(ConcertSeat seat);

    Optional<ConcertSeat> findById(Long seatId);
}
