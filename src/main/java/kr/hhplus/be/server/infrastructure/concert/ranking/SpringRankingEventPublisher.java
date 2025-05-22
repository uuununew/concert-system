package kr.hhplus.be.server.infrastructure.concert.ranking;

import kr.hhplus.be.server.domain.concert.ranking.ConcertSoldOutEvent;
import kr.hhplus.be.server.domain.concert.ranking.RankingEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SpringRankingEventPublisher implements RankingEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishConcertSoldOut(Long concertId, LocalDateTime openTime, LocalDateTime soldOutTime) {
        eventPublisher.publishEvent(new ConcertSoldOutEvent(concertId, openTime, soldOutTime));
    }
}