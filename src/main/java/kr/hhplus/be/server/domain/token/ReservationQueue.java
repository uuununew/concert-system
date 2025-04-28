package kr.hhplus.be.server.domain.token;

import java.time.Clock;
import java.util.Optional;

public class ReservationQueue {

    private final TokenRepository queueRepository;

    private final Clock clock;

    public ReservationQueue(TokenRepository queueRepository, Clock clock) {

        this.queueRepository = queueRepository;
        this.clock = clock;
    }

    public QueueToken enter(Long userId) {
        return queueRepository.enqueue(userId, clock);
    }

    public Optional<QueueToken> status(Long userId) {
        return queueRepository.findByUserId(userId);
    }

    public void leave(Long userId) {
        queueRepository.delete(userId);
    }

    public boolean canEnter(int limit) {
        return queueRepository.size() < limit;
    }
}
