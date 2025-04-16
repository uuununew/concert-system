package kr.hhplus.be.server.application.token.integration;

import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.application.token.TokenQueryService;
import kr.hhplus.be.server.config.TestContainerConfig;
import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
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
        QueueToken token = tokenRepository.enqueue(userId);

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
}
