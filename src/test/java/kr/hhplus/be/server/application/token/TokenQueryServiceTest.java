package kr.hhplus.be.server.application.token;

import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
import kr.hhplus.be.server.presentation.token.QueueTokenStatusResponse;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenQueryServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenQueryService tokenQueryService;

    @Test
    @DisplayName("대기 중인 사용자의 대기열 순서를 반환한다")
    void get_waiting_position_success() {
        // given
        Long userId = 1L;
        int expectedPosition = 3;

        when(tokenRepository.getWaitingPosition(userId))
                .thenReturn(Optional.of(expectedPosition));

        // when
        Optional<Integer> result = tokenQueryService.getWaitingPosition(userId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedPosition);
    }

    @DisplayName("대기열에 없는 userId일 경우 예외가 발생한다")
    @Test
    void getWaitingPosition_should_throw_exception_when_user_not_found() {
        // given
        Long userId = 99L;
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> tokenQueryService.getTokenStatus(userId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("토큰이 존재하지 않습니다.");
    }

    @DisplayName("WAITING 상태가 아닌 유저의 순위는 0으로 반환된다")
    @Test
    void getWaitingPosition_should_return_minus_one_when_not_waiting() {
        // given
        Long userId = 1L;
        QueueToken token = new QueueToken(userId, LocalDateTime.now());
        token.activate(); // ACTIVE 상태로 변경

        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(token));
        when(tokenRepository.getWaitingPosition(userId)).thenReturn(Optional.empty());

        // when
        QueueTokenStatusResponse result = tokenQueryService.getTokenStatus(userId);

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.status()).isEqualTo(TokenStatus.ACTIVE);
        assertThat(result.position()).isEqualTo(0);
    }
}
