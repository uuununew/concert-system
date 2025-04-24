package kr.hhplus.be.server.infrastructure.token;

import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TokenRepositoryImpl implements TokenRepository {

    private final QueueTokenJpaRepository jpaRepository;

    @Override
    public QueueToken enqueue(Long userId, Clock clock) {
        QueueToken token = QueueToken.create(userId, clock);
        return jpaRepository.save(token);
    }

    @Override
    public Optional<QueueToken> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public void delete(Long userId) {
        jpaRepository.findByUserId(userId).ifPresent(jpaRepository::delete);
    }

    @Override
    public int size() {
        return (int) jpaRepository.count();
    }

    @Override
    public List<QueueToken> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public QueueToken save(QueueToken token) {
        return jpaRepository.save(token);
    }

    @Override
    public Optional<Integer> getWaitingPosition(Long userId) {
        List<QueueToken> waitingList = findAllWaiting();

        for (int i = 0; i < waitingList.size(); i++) {
            if (waitingList.get(i).getUserId().equals(userId)) {
                return Optional.of(i + 1);
            }
        }

        return Optional.empty();
    }

    @Override
    public List<QueueToken> findAllWaiting() {
        return jpaRepository.findAllByStatusOrderByIssuedAtAsc(TokenStatus.WAITING);
    }

    @Override
    public List<QueueToken> findAllByStatusAndIssuedAtBefore(TokenStatus status, LocalDateTime before) {
        return jpaRepository.findAllByStatusAndIssuedAtBefore(status, before);
    }

    @Override
    public List<QueueToken> findAllByStatusOrderByIssuedAt(TokenStatus status) {
        return jpaRepository.findAllByStatusOrderByIssuedAt(status);
    }
}
