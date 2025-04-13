package kr.hhplus.be.server.domain.token;

import java.time.LocalDateTime;

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

    public void expireOverdueTokens(int expireMinutes, LocalDateTime now) {
        for (QueueToken token : repository.findAll()) {
            if (token.isExpired(now, expireMinutes)) {
                token.expire();
            }
        }
    }

    public int countActiveTokens() {
        return (int) repository.findAll().stream()
                .filter(QueueToken::isActive)
                .count();
    }
}