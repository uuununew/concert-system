package kr.hhplus.be.server.infrastructure.concert;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConcertSeatJpaRepository extends JpaRepository<ConcertSeat, Long> {
    List<ConcertSeat> findAllByConcert_Id(Long concertId);
    List<ConcertSeat> findAllByConcert_IdAndStatus(Long concertId, SeatStatus status);
    boolean existsByConcert_Id(Long concertId);

    //낙관적 락
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT cs FROM ConcertSeat cs WHERE cs.id = :id")
    Optional<ConcertSeat> findByIdWithOptimistic(@Param("id") Long id);
}
