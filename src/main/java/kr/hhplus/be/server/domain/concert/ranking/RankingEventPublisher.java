package kr.hhplus.be.server.domain.concert.ranking;

import java.time.LocalDateTime;

public interface RankingEventPublisher {
    void publishConcertSoldOut(Long concertId, LocalDateTime openTime, LocalDateTime soldOutTime);
}
