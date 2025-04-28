package kr.hhplus.be.server.domain.token;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TokenManager
 * - TokenRepository를 통해 대기열 전체 상태를 관리하는 도메인 서비스
 *
 * 주요 기능:
 * - expireOverdueTokens: 유효시간을 초과한 토큰들을 EXPIRED 상태로 처리
 * - countActiveTokens: 현재 ACTIVE 상태인 토큰 수 카운트
 *
 * 단위 테스트 가능하며 외부 의존 없이 순수 도메인 계산만 수행
 */
public class TokenManager {
    private final TokenRepository repository;

    public TokenManager(TokenRepository repository) {
        this.repository = repository;
    }

    //토큰 만료
    public void expireOverdueTokens(int expireMinutes, LocalDateTime now) {
        for (QueueToken token : repository.findAll()) {
            if (token.getStatus() == TokenStatus.WAITING && token.isExpired(now, expireMinutes)) {
                token.expire();
            }
        }
    }

    public int countActiveTokens() {
        return (int) repository.findAll().stream()
                .filter(QueueToken::isActive)
                .count();
    }

    //토큰 활성화
    public void activateTokens(LocalDateTime now) {
        repository.findAllByStatusAndIssuedAtBefore(TokenStatus.WAITING, now.minusMinutes(3))
                .stream()
                .findFirst()
                .ifPresent(QueueToken::activate);
    }

    /**
     * 큐에서 가장 먼저 대기 중인 사용자 토큰만 ACTIVE로 전환
     */
    public void activateNextTokenInQueue() {
        List<QueueToken> waitingTokens = repository.findAllByStatusOrderByIssuedAt(TokenStatus.WAITING);
        if (!waitingTokens.isEmpty()) {
            QueueToken next = waitingTokens.get(0); // FIFO
            next.activate();
            repository.save(next);
        }
    }
}