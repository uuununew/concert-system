package kr.hhplus.be.server.application.token;

import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenManager;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
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

    @Transactional
    public QueueToken issue(Long userId) {
        Optional<QueueToken> optionalToken = tokenRepository.findByUserId(userId);

        if (optionalToken.isPresent()) {
            QueueToken token = optionalToken.get();

            // 만료 검증
            if (!token.isExpired(LocalDateTime.now(clock), expireMinutes)
                    && token.isWaitingOrActive()) { // WAITING or ACTIVE 상태라면
                return token; // 재사용
            }

            token.expire();
        }

        // 새 토큰 발급
        return tokenRepository.enqueue(userId, clock);
    }

    public void activate(Long userId) {
        QueueToken token = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TOKEN_NOT_FOUND, "토큰 정보가 없습니다."));

        if (token.getStatus() == TokenStatus.ACTIVE) {
            return; // 이미 ACTIVE면 추가 로직 없이 통과
        }

        if (token.isExpired(LocalDateTime.now(clock), expireMinutes)) {
            token.expire();
            tokenRepository.save(token);
            throw new CustomException(ErrorCode.TOKEN_NOT_FOUND, "토큰 정보가 없습니다.");
        }

        // 순서 검증: 대기열 가장 앞에 있는 유저인지 확인
        List<QueueToken> waitingTokens = tokenRepository.findAllByStatusOrderByIssuedAt(TokenStatus.WAITING);
        if (waitingTokens.isEmpty() || !waitingTokens.get(0).getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN_ORDER);
        }

        token.activate();
        tokenRepository.save(token);
    }

    public void complete(Long userId) {
        QueueToken token = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TOKEN_NOT_FOUND, "토큰 정보가 없습니다."));
        token.expire();
        tokenRepository.save(token);
    }

    public void restore(Long userId) {
        QueueToken token = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TOKEN_NOT_FOUND, "토큰 정보가 없습니다."));
        token.restore();
        tokenRepository.save(token);
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

    public void activateEligibleTokens() {
        tokenManager.activateTokens(LocalDateTime.now(clock));
    }

}
