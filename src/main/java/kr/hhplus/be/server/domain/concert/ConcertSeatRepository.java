package kr.hhplus.be.server.domain.concert;

import java.util.List;

public interface ConcertSeatRepository {
    List<ConcertSeat> findAllByConcertId(Long concertId);
}
