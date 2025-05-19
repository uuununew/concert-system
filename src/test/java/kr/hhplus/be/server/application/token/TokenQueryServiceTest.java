package kr.hhplus.be.server.application.token;

import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
import kr.hhplus.be.server.presentation.token.QueueTokenStatusResponse;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenQueryServiceTest {

    private TokenRepository tokenRepository;
    private TokenQueryService tokenQueryService;

    @BeforeEach
    void setUp() {
        tokenRepository = mock(TokenRepository.class);
        tokenQueryService = new TokenQueryService(tokenRepository);
    }

    @Test
    @DisplayName("토큰 순번이 0이면 ACTIVE 상태로 간주")
    void getTokenStatus_shouldReturnActive_ifPositionIsZero() {
        // given
        String tokenId = "token-1";
        when(tokenRepository.getWaitingPosition(tokenId)).thenReturn(Optional.of(0));

        // when
        QueueTokenStatusResponse result = tokenQueryService.getTokenStatus(tokenId);

        // then
        assertThat(result.tokenId()).isEqualTo(tokenId);
        assertThat(result.status()).isEqualTo(TokenStatus.ACTIVE);
        assertThat(result.position()).isEqualTo(0);
    }

    @Test
    @DisplayName("토큰 순번이 1 이상이면 WAITING 상태로 간주")
    void getTokenStatus_shouldReturnWaiting_ifPositionIsGreaterThanZero() {
        // given
        String tokenId = "token-2";
        when(tokenRepository.getWaitingPosition(tokenId)).thenReturn(Optional.of(5));

        // when
        QueueTokenStatusResponse result = tokenQueryService.getTokenStatus(tokenId);

        // then
        assertThat(result.tokenId()).isEqualTo(tokenId);
        assertThat(result.status()).isEqualTo(TokenStatus.WAITING);
        assertThat(result.position()).isEqualTo(5);
    }

    @Test
    @DisplayName("토큰이 존재하지 않으면 NOT_FOUND로 간주")
    void getTokenStatus_shouldReturnNotFound_ifTokenDoesNotExist() {
        // given
        String tokenId = "not-found";
        when(tokenRepository.getWaitingPosition(tokenId)).thenReturn(Optional.empty());

        // when
        QueueTokenStatusResponse result = tokenQueryService.getTokenStatus(tokenId);

        // then
        assertThat(result.tokenId()).isEqualTo(tokenId);
        assertThat(result.status()).isEqualTo(TokenStatus.EXPIRED);
        assertThat(result.position()).isNull();
    }
}
