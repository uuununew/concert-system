package kr.hhplus.be.server.infrastructure.concert.reservation.token;

import kr.hhplus.be.server.domain.concert.reservation.token.QueueToken;
import kr.hhplus.be.server.domain.concert.reservation.token.TokenRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTokenRepository implements TokenRepository {

    private final Map<Long, QueueToken> store = new ConcurrentHashMap<>();

    @Override
    public QueueToken enqueue(Long userId) {
        QueueToken token = new QueueToken(userId, LocalDateTime.now());
        return save(token);
    }

    @Override
    public Optional<QueueToken> findByUserId(Long userId) {
        return Optional.ofNullable(store.get(userId));
    }

    @Override
    public void delete(Long userId) {
        store.remove(userId);
    }

    @Override
    public int size() {
        return store.size();
    }

    @Override
    public List<QueueToken> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public QueueToken save(QueueToken token) {
        store.put(token.getUserId(), token);
        return token;
    }

}
