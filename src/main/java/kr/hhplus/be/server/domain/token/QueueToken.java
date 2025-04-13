package kr.hhplus.be.server.domain.token;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 유저의 대기열 토큰 도메인 객체입니다.
 * - 발급 시 WAITING 상태로 시작합니다.
 * - 이후 activate(), expire(), restore() 메서드를 통해 상태 전이를 담당합니다.
 * - isExpired()를 통해 만료 여부를 판단하며
 * - isActive()를 통해 활성 상태인지 확인할 수 있습니다.
 */
@Getter
public class QueueToken {

    private final Long userId;
    private final LocalDateTime issuedAt;
    private TokenStatus status;

    public QueueToken(Long userId, LocalDateTime issuedAt) {
        this.userId = userId;
        this.issuedAt = issuedAt;
        this.status = TokenStatus.WAITING;
    }

    public void activate() {
        this.status = TokenStatus.ACTIVE;
    }

    public void expire() {
        this.status = TokenStatus.EXPIRED;
    }

    public void restore() {
        this.status = TokenStatus.ACTIVE;
    }

    public boolean isExpired(LocalDateTime now, int expireMinutes) {
        return issuedAt.plusMinutes(expireMinutes).isBefore(now);
    }

    public boolean isActive() {
        return this.status == TokenStatus.ACTIVE;
    }
}
