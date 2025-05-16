package kr.hhplus.be.server.domain.token;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

public class ReservationQueue {

    private final TokenRepository queueRepository;
    private final Clock clock;

    public ReservationQueue(TokenRepository queueRepository, Clock clock) {
        this.queueRepository = queueRepository;
        this.clock = clock;
    }

    /**
     * 대기열 진입 - tokenId 생성 및 등록
     */
    public String enter(Long userId) {
        String tokenId = UUID.randomUUID().toString();
        long score = LocalDateTime.now(clock).toEpochSecond(ZoneOffset.UTC);
        queueRepository.enqueue(tokenId, score);
        return tokenId;
    }


    /**
     * 대기열에서 나가기
     */
    public void leave(String tokenId) {
        queueRepository.delete(tokenId);
    }

}
