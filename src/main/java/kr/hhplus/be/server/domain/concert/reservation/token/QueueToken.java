package kr.hhplus.be.server.domain.concert.reservation.token;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

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
