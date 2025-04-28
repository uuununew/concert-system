package kr.hhplus.be.server.infrastructure.concert;

import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.ConcertSeatRepository;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ConcertSeatRepositoryImpl implements ConcertSeatRepository {

    private final ConcertSeatJpaRepository concertSeatJpaRepository;

    @Override
    public List<ConcertSeat> findAllByConcert_Id(Long concertId) {
        return concertSeatJpaRepository.findAllByConcert_Id(concertId);
    }

    @Override
    public List<ConcertSeat> findAllByConcert_IdAndStatus(Long concertId, SeatStatus status) {
        return concertSeatJpaRepository.findAllByConcert_IdAndStatus(concertId, status);
    }

    @Override
    public ConcertSeat save(ConcertSeat seat) {
        return concertSeatJpaRepository.save(seat);
    }

    @Override
    public Optional<ConcertSeat> findById(Long id) {
        return concertSeatJpaRepository.findById(id);
    }

    @Override
    public boolean existsByConcert_Id(Long concertId) {
        return concertSeatJpaRepository.existsByConcert_Id(concertId);
    }

    @Override
    public Optional<ConcertSeat> findByIdWithOptimistic(Long id) {
        return concertSeatJpaRepository.findByIdWithOptimistic(id);
    }
}
