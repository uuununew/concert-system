package kr.hhplus.be.server.domain.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TokenManagerTest {

    TokenRepository tokenRepository;
    TokenManager tokenManager;

    @BeforeEach
    void setUp() {
        tokenRepository = mock(TokenRepository.class);
        tokenManager = new TokenManager(tokenRepository);
    }

    @Test
    @DisplayName("score가 기준치보다 낮은 토큰은 만료 처리된다")
    void expireOverdueTokens_shouldCallRepositoryWithCorrectThreshold() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 5, 15, 12, 0, 0);
        int expireMinutes = 10;
        long expectedThreshold = now.minusMinutes(expireMinutes).toEpochSecond(ZoneOffset.UTC);

        // when
        tokenManager.expireOverdueTokens(expireMinutes, now);

        // then
        verify(tokenRepository).expireTokensBefore(expectedThreshold);
    }
}
