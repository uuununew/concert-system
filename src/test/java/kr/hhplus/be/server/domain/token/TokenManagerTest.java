package kr.hhplus.be.server.domain.token;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TokenManagerTest {

    @DisplayName("대기 중인 토큰 중 가장 먼저 발급된 하나를 ACTIVE 상태로 전환한다.")
    @Test
    void activateTokens_should_activate_earliest_waiting_token() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // 대상이 되는 토큰
        QueueToken tokenToActivate = new QueueToken(1L, now.minusMinutes(10));
        tokenToActivate.restore(); // WAITING 상태

        List<QueueToken> eligibleTokens = List.of(tokenToActivate);

        TokenRepository mockRepository = mock(TokenRepository.class);
        when(mockRepository.findAllByStatusAndIssuedAtBefore(TokenStatus.WAITING, now.minusMinutes(3)))
                .thenReturn(eligibleTokens);

        TokenManager tokenManager = new TokenManager(mockRepository);

        // when
        tokenManager.activateTokens(now);

        // then
        assertThat(tokenToActivate.getStatus()).isEqualTo(TokenStatus.ACTIVE);
    }
}
