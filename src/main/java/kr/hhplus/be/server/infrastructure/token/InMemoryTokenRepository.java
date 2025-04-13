package kr.hhplus.be.server.infrastructure.token;

import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;

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

    @Override
    public int getWaitingPosition(Long userId) {
        List<QueueToken> waitingList = findAllWaiting();

        for (int i = 0; i < waitingList.size(); i++) {
            if (waitingList.get(i).getUserId().equals(userId)) {
                return i + 1;
            }
        }
        // userId가 대기열에 없는 경우
        throw new IllegalArgumentException("대기열에 해당 사용자가 존재하지 않습니다.");
    }

    @Override
    public List<QueueToken> findAllWaiting() {
        return store.values().stream()
                .filter(token -> token.getStatus() == TokenStatus.WAITING)
                .sorted((a, b) -> a.getIssuedAt().compareTo(b.getIssuedAt()))
                .toList();
    }

}
