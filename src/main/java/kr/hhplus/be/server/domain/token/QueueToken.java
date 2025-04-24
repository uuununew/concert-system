package kr.hhplus.be.server.domain.token;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 유저의 대기열 토큰 도메인 객체입니다.
 * - 발급 시 WAITING 상태로 시작합니다.
 * - 이후 activate(), expire(), restore() 메서드를 통해 상태 전이를 담당합니다.
 * - isExpired()를 통해 만료 여부를 판단하며
 * - isActive()를 통해 활성 상태인지 확인할 수 있습니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QueueToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private TokenStatus status;

    private LocalDateTime issuedAt;

    public QueueToken(Long userId, LocalDateTime issuedAt) {
        this.userId = userId;
        this.issuedAt = issuedAt;
        this.status = TokenStatus.WAITING;
    }

    // 정적 팩토리 메서드
    public static QueueToken create(Long userId, Clock clock) {
        QueueToken token = new QueueToken();
        token.userId = userId;
        token.issuedAt = LocalDateTime.now(clock);
        token.status = TokenStatus.WAITING;
        return token;
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
