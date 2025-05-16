package kr.hhplus.be.server.domain.token;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class TokenManager {

    private final TokenRepository repository;

    public TokenManager(TokenRepository repository) {
        this.repository = repository;
    }

    /**
     * Redis ZSet에서 score(timestamp) 기준으로 유효시간 지난 토큰 제거
     */
    public void expireOverdueTokens(int expireMinutes, LocalDateTime now) {
        long threshold = now.minusMinutes(expireMinutes).toEpochSecond(ZoneOffset.UTC);
        repository.expireTokensBefore(threshold);
    }

    /**
     * 현재 ACTIVE 상태 토큰 수 → Redis 구조에서는 선두만 ACTIVE로 간주되므로 항상 최대 1
     */
    public int countActiveTokens() {
        // Redis 구조에서는 선두 토큰만 active 간주
        // 실제로는 이 메서드 필요 없어질 수도 있음
        return 1;  // 또는 필요 시 0 or 1 판단 로직 구성
    }

    /**
     * 별도 활성화 불필요 — Redis 구조에서는 ZSet 순번이 곧 상태
     */
    public void activateNextTokenInQueue() {
        // 불필요. ZRank 0이면 선두로 간주
    }
}