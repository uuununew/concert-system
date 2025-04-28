package kr.hhplus.be.server.domain.token;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TokenRepository {

    QueueToken save(QueueToken token);

    // 현재 토큰 상태 조회
    Optional<QueueToken> findByUserId(Long userId);

    // 유저 대기열 이탈
    void delete(Long userId);

    int size();

    List<QueueToken> findAll();

    // 단건 조회
    QueueToken enqueue(Long userId, Clock clock);

    List<QueueToken> findAllWaiting();

    // 해당 userId의 WAITING 순서 조회
    Optional<Integer> getWaitingPosition(Long userId);

    List<QueueToken> findAllByStatusAndIssuedAtBefore(TokenStatus status, LocalDateTime before);

    List<QueueToken> findAllByStatusOrderByIssuedAt(TokenStatus status);

}
