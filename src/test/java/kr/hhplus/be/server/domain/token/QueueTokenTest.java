package kr.hhplus.be.server.domain.token;

import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class QueueTokenTest {

    @Test
    @DisplayName("activate 호출 시 상태가 ACTIVE로 변경된다")
    void activate_should_set_status_active() {
        // given : WAITING 상태의 토큰 생성
        QueueToken token = new QueueToken(1L, LocalDateTime.now());

        // when : 토큰을 활성화
        token.activate();

        // then : 상태가 ACTIVE로 변경됨
        assertEquals(TokenStatus.ACTIVE, token.getStatus());
    }

    @Test
    @DisplayName("expire 호출 시 상태가 EXPIRED로 변경된다")
    void expire_should_set_status_expired() {
        // given : 활성화된 토큰
        QueueToken token = new QueueToken(1L, LocalDateTime.now());
        token.activate();

        // when : expire 호출
        token.expire();

        // then : 상태가 EXPIRED로 변경됨
        assertEquals(TokenStatus.EXPIRED, token.getStatus());
    }

    @Test
    @DisplayName("restore 호출 시 상태가 ACTIVE로 복구된다")
    void restore_should_set_status_active() {
        // given : 만료된 토큰
        QueueToken token = new QueueToken(1L, LocalDateTime.now());
        token.expire();

        // when : 복구 호출
        token.restore();

        // then : 상태가 ACTIVE로 복구됨
        assertEquals(TokenStatus.ACTIVE, token.getStatus());
    }

    @Test
    @DisplayName("만료 시간이 경과된 경우 isExpired는 true를 반환한다")
    void isExpired_should_return_true_if_time_exceeded() {
        // given : 10분 전에 발급된 토큰
        LocalDateTime issuedAt = LocalDateTime.now().minusMinutes(10);
        QueueToken token = new QueueToken(1L, issuedAt);

        // when : 현재 시각 기준 만료 여부 확인 (기한 5분)
        boolean expired = token.isExpired(LocalDateTime.now(), 5);

        // then : 만료로 간주됨
        assertTrue(expired);
    }

    @Test
    @DisplayName("만료 시간이 지나지 않은 경우 isExpired는 false를 반환한다")
    void isExpired_should_return_false_if_within_time() {
        // given : 3분 전에 발급된 토큰
        LocalDateTime issuedAt = LocalDateTime.now().minusMinutes(3);
        QueueToken token = new QueueToken(1L, issuedAt);

        // when : 현재 시각 기준 만료 여부 확인 (기한 5분)
        boolean expired = token.isExpired(LocalDateTime.now(), 5);

        // then : 아직 만료되지 않음
        assertFalse(expired);
    }
}
