package kr.hhplus.be.server.infrastructure.concert;

import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConcertSeatJpaRepository extends JpaRepository<ConcertSeat, Long> {
    List<ConcertSeat> findAllByConcert_Id(Long concertId);
    List<ConcertSeat> findAllByConcert_IdAndStatus(Long concertId, SeatStatus status);
    boolean existsByConcert_Id(Long concertId);
}
