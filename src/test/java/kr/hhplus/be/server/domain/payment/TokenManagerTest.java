package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenManager;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenManagerTest {

    @Mock
    TokenRepository tokenRepository;

    TokenManager tokenManager;

    @BeforeEach
    void setUp() {
        tokenManager = new TokenManager(tokenRepository);
    }

    @Test
    @DisplayName("만료 기준 시간을 초과한 WAITING 토큰만 EXPIRED 상태로 변경된다")
    void expire_only_expired_tokens() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 4, 12, 12, 0);
        QueueToken expired = new QueueToken(1L, now.minusMinutes(6));
        QueueToken valid = new QueueToken(2L, now.minusMinutes(3));

        when(tokenRepository.findAll()).thenReturn(List.of(expired, valid));

        // when
        tokenManager.expireOverdueTokens(5, now);

        // then
        assertThat(expired.getStatus()).isEqualTo(TokenStatus.EXPIRED);
        assertThat(valid.getStatus()).isEqualTo(TokenStatus.WAITING);
    }

    @Test
    @DisplayName("이미 EXPIRED 상태인 토큰은 무시된다")
    void skip_already_expired_token() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 4, 12, 12, 0);
        QueueToken token = new QueueToken(1L, now.minusMinutes(10));
        token.expire(); // 이미 만료 상태

        when(tokenRepository.findAll()).thenReturn(List.of(token));

        // when
        tokenManager.expireOverdueTokens(5, now);

        // then
        assertThat(token.getStatus()).isEqualTo(TokenStatus.EXPIRED);
    }

    @Test
    @DisplayName("활성화된 토큰은 만료 대상에서 제외된다")
    void skip_active_tokens() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 4, 12, 12, 0);
        QueueToken token = new QueueToken(1L, now.minusMinutes(10));
        token.activate();

        when(tokenRepository.findAll()).thenReturn(List.of(token));

        // when
        tokenManager.expireOverdueTokens(5, now);

        // then
        assertThat(token.getStatus()).isEqualTo(TokenStatus.ACTIVE);
    }
}
