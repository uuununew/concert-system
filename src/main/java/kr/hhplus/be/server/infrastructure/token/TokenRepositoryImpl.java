package kr.hhplus.be.server.infrastructure.token;

import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TokenRepositoryImpl implements TokenRepository {

    private final QueueTokenJpaRepository jpaRepository;

    @Override
    public QueueToken enqueue(Long userId) {
        QueueToken token = QueueToken.create(userId);
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
    public int getWaitingPosition(Long userId) {
        List<QueueToken> waitingList = findAllWaiting();

        for (int i = 0; i < waitingList.size(); i++) {
            if (waitingList.get(i).getUserId().equals(userId)) {
                return i + 1;
            }
        }
        throw new IllegalArgumentException("대기열에 해당 사용자가 존재하지 않습니다.");
    }

    @Override
    public List<QueueToken> findAllWaiting() {
        return jpaRepository.findAllByStatusOrderByIssuedAtAsc(TokenStatus.WAITING);
    }
}
