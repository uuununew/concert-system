package kr.hhplus.be.server.application.token;

import kr.hhplus.be.server.domain.token.TokenManager;
import kr.hhplus.be.server.domain.token.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.*;
import java.util.Optional;
import java.util.Set;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenCommandServiceTest{

    private TokenRepository tokenRepository;
    private TokenManager tokenManager;
    private Clock clock;
    private TokenCommandService tokenCommandService;
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        tokenRepository = mock(TokenRepository.class);
        tokenManager = mock(TokenManager.class);
        clock = Clock.fixed(Instant.parse("2025-05-15T12:00:00Z"), ZoneOffset.UTC);
        redisTemplate = mock(RedisTemplate.class);
        tokenCommandService = new TokenCommandService(tokenRepository, clock, redisTemplate,10);
    }

    @Test
    @DisplayName("토큰 발급 시 UUID 생성 및 score 저장")
    void issue_shouldGenerateTokenAndSaveToRedis() {
        // when
        String tokenId = tokenCommandService.issue(1L);

        // then
        verify(tokenRepository, times(1)).enqueue(eq(tokenId), anyLong());
    }

    @Test
    @DisplayName("상위 토큰들이 자동 활성화된다")
    void activateEligibleTokens_shouldDeleteTopTokens() {
        // given
        Set<String> topTokens = Set.of("tokenA", "tokenB", "tokenC");
        when(tokenRepository.findTopTokens(3)).thenReturn(topTokens);

        // when
        tokenCommandService.activateEligibleTokens(3);

        // then
        for (String token : topTokens) {
            verify(tokenRepository).delete(token);
        }
    }

    @Test
    @DisplayName("만료 기준 이전 토큰은 삭제된다")
    void expireOverdueTokens_shouldCallRepository() {
        // when
        tokenCommandService.expireOverdueTokens();

        // then
        long expectedThreshold = LocalDateTime.now(clock).minusMinutes(10).toEpochSecond(ZoneOffset.UTC);
        verify(tokenRepository).expireTokensBefore(expectedThreshold);
    }

    @Test
    @DisplayName("토큰 완료 시 삭제된다")
    void complete_shouldRemoveToken() {
        // given
        String tokenId = "token-99";

        // when
        tokenCommandService.complete(tokenId);

        // then
        verify(tokenRepository).delete(tokenId);
    }
}
