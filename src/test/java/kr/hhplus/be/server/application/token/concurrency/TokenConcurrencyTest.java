package kr.hhplus.be.server.application.token.concurrency;

import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.config.TestContainerConfig;
import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.support.concurrency.ConcurrencyTestExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class TokenConcurrencyTest extends TestContainerConfig {

    @Autowired
    private TokenCommandService tokenCommandService;

    @Autowired
    private TokenRepository tokenRepository;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        // 테스트 시작 전 토큰 삭제 (중복 방지)
        tokenRepository.findByUserId(userId).ifPresent(t -> tokenRepository.delete(userId));
    }

    @DisplayName("동시에 토큰 발급 요청이 들어와도 하나만 생성되어야 한다.")
    @Test
    void issueToken_concurrent_onlyOne_Created() throws InterruptedException {
        // given
        int threadCount = 10;
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        // when
        ConcurrencyTestExecutor.run(threadCount, () -> {
            try {
                tokenCommandService.issue(userId);
            } catch (Throwable t) {
                exceptions.add(t);
            }
        });

        // then
        List<QueueToken> allTokens = tokenRepository.findAll();
        long userTokenCount = allTokens.stream()
                .filter(token -> token.getUserId().equals(userId))
                .count();

        assertThat(userTokenCount).isEqualTo(1);
    }
}
