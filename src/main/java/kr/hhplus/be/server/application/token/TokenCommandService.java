package kr.hhplus.be.server.application.token;

import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenManager;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * TokenCommandService
 * - 유저의 대기열 토큰을 발급하고 상태를 관리하는 유즈케이스 클래스
 * - 외부 요청 시점의 비즈니스 유효성 검증과 상태 전이를 담당
 *
 * 주요 기능:
 * - issue: 새 토큰 발급 (WAITING 상태)
 * - activate: 유효성 검증 후 ACTIVE로 변경 (만료 검증 포함)
 * - complete: 예약 완료 후 토큰을 EXPIRED 처리
 * - restore: 예약 취소 시 토큰을 재사용 가능 상태로 복원
 * - expireOverdueTokens: 스케줄러용 - 유효시간 초과 토큰 일괄 만료
 */
public class TokenCommandService {

    private final TokenRepository tokenRepository;
    private final TokenManager tokenManager;
    private final Clock clock;
    private final int expireMinutes;

    public TokenCommandService(TokenRepository tokenRepository, TokenManager tokenManager, Clock clock, int expireMinutes) {
        this.tokenRepository = tokenRepository;
        this.tokenManager = tokenManager;
        this.clock = clock;
        this.expireMinutes = expireMinutes;
    }

    public QueueToken issue(Long userId) {
        return tokenRepository.findByUserId(userId)
                .orElseGet(() -> {
                    QueueToken newToken = new QueueToken(userId, LocalDateTime.now(clock));
                    return tokenRepository.save(newToken);
                });
    }

    public void activate(Long userId) {
        QueueToken token = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "토큰 정보가 없습니다."));
        if (token.isExpired(LocalDateTime.now(clock), expireMinutes)) {
            token.expire();
            throw new CustomException(ErrorCode.NOT_FOUND, "토큰 정보가 없습니다.");
        }
        token.activate();
    }

    public void complete(Long userId) {
        QueueToken token = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "토큰 정보가 없습니다."));
        token.expire();
    }

    public void restore(Long userId) {
        QueueToken token = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "토큰 정보가 없습니다."));
        token.restore();
    }

    /**
     * 유저의 토큰 상태를 조회합니다.
     * @param userId 유저 식별자
     * @return Optional<QueueToken> 존재하면 반환, 없으면 empty
     */
    public Optional<QueueToken> status(Long userId) {
        return tokenRepository.findByUserId(userId);
    }

    public void expireOverdueTokens() {
        tokenManager.expireOverdueTokens(expireMinutes, LocalDateTime.now(clock));
    }

}
