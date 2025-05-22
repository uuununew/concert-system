package kr.hhplus.be.server.infrastructure.concert.ranking;

import kr.hhplus.be.server.domain.concert.ranking.DailyConcertRanking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyConcertRankingJpaRepository extends JpaRepository<DailyConcertRanking, Long> {

    List<DailyConcertRanking> findAllByRankingDate(LocalDate rankingDate);

    boolean existsByConcertIdAndRankingDate(Long concertId, LocalDate rankingDate);
}
