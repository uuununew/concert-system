package kr.hhplus.be.server.domain.concert.ranking;

import java.time.LocalDateTime;

public record ConcertRankingDetail(
        Long concertId,
        String title,
        LocalDateTime concertDateTime,
        Double soldOutDurationMillis
) {}
