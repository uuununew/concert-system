package kr.hhplus.be.server.application.token.integration;

import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.application.token.TokenQueryService;
import kr.hhplus.be.server.config.TestContainerConfig;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Testcontainers
public class TokenIntegrationTest extends TestContainerConfig {
    @Autowired
    TokenCommandService tokenCommandService;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    Clock clock;

    private static final String QUEUE_KEY = "reservation:queue";
    private static final String TTL_PREFIX = "reservation:token:EXPIRE:";

    @BeforeEach
    void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @Test
    @DisplayName("토큰 발급 후 Redis SortedSet과 TTL 키가 모두 저장된다")
    void issue_token_and_store_in_redis() {
        // when
        String tokenId = tokenCommandService.issue(1L);

        // then
        Set<String> tokenIds = redisTemplate.opsForZSet().range(QUEUE_KEY, 0, -1);
        String ttlKey = TTL_PREFIX + tokenId;
        Boolean ttlExists = redisTemplate.hasKey(ttlKey);

        assertThat(tokenIds).contains(tokenId);
        assertThat(ttlExists).isTrue();
    }


    @Test
    @DisplayName("TTL이 만료되면 ZSet에 남아 있어도 expireTokensBefore로 제거된다")
    void expire_token_by_ttl_check() {
        // given
        String tokenId = tokenCommandService.issue(1L);
        long oldScore = LocalDateTime.now(clock).minusMinutes(20).toEpochSecond(ZoneOffset.UTC);
        redisTemplate.opsForZSet().add(QUEUE_KEY, tokenId, oldScore);

        redisTemplate.delete(TTL_PREFIX + tokenId); // TTL 만료 시뮬레이션

        // when
        tokenCommandService.expireOverdueTokens();

        // then
        Set<String> remaining = redisTemplate.opsForZSet().range(QUEUE_KEY, 0, -1);
        assertThat(remaining).doesNotContain(tokenId);
    }

    @Test
    @DisplayName("activateEligibleTokens 호출 시 상위 N개 토큰이 제거된다")
    void activate_top_tokens_should_be_deleted() throws InterruptedException {
        // given
        String token1 = tokenCommandService.issue(1L);
        Thread.sleep(10);
        String token2 = tokenCommandService.issue(2L);
        Thread.sleep(10);
        String token3 = tokenCommandService.issue(3L);

        // when
        tokenCommandService.activateEligibleTokens(2);

        // then
        Set<String> remaining = redisTemplate.opsForZSet().range(QUEUE_KEY, 0, -1);

        assertThat(remaining).contains(token3);
        assertThat(remaining).doesNotContain(token1, token2);
    }
}
