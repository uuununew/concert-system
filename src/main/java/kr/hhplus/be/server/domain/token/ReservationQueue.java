package kr.hhplus.be.server.domain.token;

import java.util.Optional;

public class ReservationQueue {

    private final TokenRepository queueRepository;

    public ReservationQueue(TokenRepository queueRepository) {
        this.queueRepository = queueRepository;
    }

    public QueueToken enter(Long userId) {
        return queueRepository.enqueue(userId);
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
