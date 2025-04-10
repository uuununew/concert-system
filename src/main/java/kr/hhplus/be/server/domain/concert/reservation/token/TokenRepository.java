package kr.hhplus.be.server.domain.concert.reservation.token;

import java.util.List;
import java.util.Optional;

public interface TokenRepository {

    // 유저 큐에 진입
    QueueToken save(QueueToken token);

    // 현재 토큰 상태 조회
    Optional<QueueToken> findByUserId(Long userId);

    // 유저 대기열 이탈
    void delete(Long userId);

    // 전체 토큰 수
    int size();

    // 전체 조회 (토큰 만료 등에 사용)
    List<QueueToken> findAll();

    // 단건 조회
    QueueToken enqueue(Long userId);

}
