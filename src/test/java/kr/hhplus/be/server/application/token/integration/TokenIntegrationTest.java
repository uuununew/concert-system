package kr.hhplus.be.server.application.token.integration;

import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.application.token.TokenQueryService;
import kr.hhplus.be.server.config.TestContainerConfig;
import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Testcontainers
public class TokenIntegrationTest extends TestContainerConfig {
    @Autowired
    TokenCommandService tokenCommandService;

    @Autowired
    TokenQueryService tokenQueryService;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    Clock clock;

    @Test
    @DisplayName("토큰 발급 후 DB에 저장된다")
    void issue_token_and_persist() {
        // given
        Long userId = 100L;

        // when
        tokenCommandService.issue(userId);

        // then
        Optional<QueueToken> result = tokenRepository.findByUserId(userId);
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TokenStatus.WAITING);
    }

    @Test
    @DisplayName("EXPIRED 기준 시간 이후에는 토큰이 만료된다")
    void expire_token_by_time() {
        // given
        Long userId = 200L;
        QueueToken token = tokenRepository.enqueue(userId, clock);

        // 만료 기준 시간보다 예전 시간으로 강제 설정
        LocalDateTime past = LocalDateTime.now(clock).minusMinutes(10);
        token.expire(); // 상태 변경

        // when
        tokenCommandService.complete(userId);

        // then
        Optional<QueueToken> result = tokenRepository.findByUserId(userId);
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(TokenStatus.EXPIRED);
    }

    @Test
    @DisplayName("대기 중 첫 번째 사용자만 정상적으로 ACTIVE 전환된다")
    void only_first_user_can_be_activated() {
        // given
        QueueToken first = tokenRepository.enqueue(1L, clock);
        QueueToken second = tokenRepository.enqueue(2L, clock);

        // when
        tokenCommandService.activate(first.getUserId());

        // then
        assertThat(tokenRepository.findByUserId(1L).get().getStatus()).isEqualTo(TokenStatus.ACTIVE);
        assertThat(tokenRepository.findByUserId(2L).get().getStatus()).isEqualTo(TokenStatus.WAITING);
    }

    @Test
    @DisplayName("차례가 아닌 사용자가 활성화 요청 시 예외가 발생한다")
    void activate_out_of_order_throws() {
        // given
        QueueToken first = tokenRepository.enqueue(1L, clock); // 먼저 들어온 사용자
        QueueToken second = tokenRepository.enqueue(2L, clock); // 나중 사용자

        // when // then
        assertThatThrownBy(() -> tokenCommandService.activate(second.getUserId()))
                .isInstanceOf(CustomException.class)
                .hasMessage("아직 차례가 아닙니다.");
    }
}
