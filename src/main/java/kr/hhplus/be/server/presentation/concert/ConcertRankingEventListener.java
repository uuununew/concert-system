package kr.hhplus.be.server.presentation.concert;

import kr.hhplus.be.server.domain.concert.ranking.ConcertSoldOutEvent;
import kr.hhplus.be.server.infrastructure.concert.ranking.ConcertSeatCountRedisRepository;
import kr.hhplus.be.server.infrastructure.concert.ranking.ConcertRankingRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.time.ZoneOffset;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConcertRankingEventListener {

    private final ConcertSeatCountRedisRepository concertSeatCountRedisRepository;
    private final ConcertRankingRedisRepository rankingRedisRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onConcertSoldOut(ConcertSoldOutEvent event) {
        long remaining = concertSeatCountRedisRepository.getRemainCount(event.concertId());
        if (remaining == 0) {
            rankingRedisRepository.saveSoldOutRanking(
                    event.concertId(),
                    event.soldOutTime().toInstant(ZoneOffset.UTC).toEpochMilli(),
                    event.concertOpenTime().toInstant(ZoneOffset.UTC).toEpochMilli()
            );
        }
    }
}
