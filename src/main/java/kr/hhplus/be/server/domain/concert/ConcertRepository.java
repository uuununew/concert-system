package kr.hhplus.be.server.domain.concert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConcertRepository extends JpaRepository<Concert, Long> {
    List<ConcertSeat> findAllByConcertId(Long concertId);
    List<ConcertSeat> findAllByConcertIdAndStatus(Long concertId, SeatStatus status);

}
