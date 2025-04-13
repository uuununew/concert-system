package kr.hhplus.be.server.application.token;

import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenManager;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TokenCommandServiceTest {

    private TokenRepository tokenRepository;
    private TokenManager tokenManager;
    private Clock clock;
    private TokenCommandService service;

    @BeforeEach
    void setUp() {
        tokenRepository = mock(TokenRepository.class);
        tokenManager = mock(TokenManager.class);
        clock = Clock.fixed(Instant.parse("2025-04-11T12:00:00Z"), ZoneOffset.UTC);
        service = new TokenCommandService(tokenRepository, tokenManager, clock, 5);
    }

    @Test
    @DisplayName("새 토큰이 없으면 발급 후 저장한다")
    void issue_new_token() {
        // given : 저장된 토큰이 없는 userId 설정
        Long userId = 1L;
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when : 토큰 발급 호출
        QueueToken token = service.issue(userId);

        // then : 새로운 토큰이 발급되고, 상태는 WAITING
        assertEquals(userId, token.getUserId());
        assertEquals(TokenStatus.WAITING, token.getStatus());
    }

    @Test
    @DisplayName("존재하지 않는 토큰을 활성화하면 예외 발생")
    void activate_without_token_throws() {
        // given : 유효하지 않은 userId로 저장된 토큰 없음
        Long userId = 100L;
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when //then : 활성화 시도 시 CustomException 발생
        assertThrows(CustomException.class, () -> service.activate(userId));
    }

    @Test
    @DisplayName("이미 토큰이 있으면 기존 것을 반환한다")
    void issue_existing_token() {
        // given : 이미 발급된 토큰이 존재하는 userId
        Long userId = 2L;
        QueueToken existing = new QueueToken(userId, LocalDateTime.now(clock));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        // when : 토큰 발급 호출
        QueueToken result = service.issue(userId);

        // then : 기존 토큰을 그대로 반환
        assertSame(existing, result);
    }

    @Test
    @DisplayName("토큰이 유효하면 활성화된다")
    void activate_valid_token() {
        // given : 아직 만료되지 않은 토큰
        Long userId = 3L;
        QueueToken token = new QueueToken(userId, LocalDateTime.now(clock).minusMinutes(2));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(token));

        // when : 토큰 활성화 시도
        service.activate(userId);

        // then : 토큰 상태가 ACTIVE로 변경
        assertEquals(TokenStatus.ACTIVE, token.getStatus());
    }

    @Test
    @DisplayName("만료된 토큰은 활성화 실패하고 상태가 EXPIRED가 된다")
    void activate_expired_token() {
        // given : 만료 기준을 초과한 오래된 토큰
        Long userId = 4L;
        QueueToken token = new QueueToken(userId, LocalDateTime.now(clock).minusMinutes(6));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(token));

        // when // then : 활성화 시도 시 CustomException 발생 및 상태 EXPIRED
        assertThrows(CustomException.class, () -> service.activate(userId));
        assertEquals(TokenStatus.EXPIRED, token.getStatus());
    }

    @Test
    @DisplayName("complete 호출 시 토큰은 EXPIRED 상태가 된다")
    void complete_token() {
        // given : 예약 완료 처리할 ACTIVE 토큰
        Long userId = 5L;
        QueueToken token = new QueueToken(userId, LocalDateTime.now(clock));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(token));

        // when : complete 호출
        service.complete(userId);

        // then : 토큰 상태가 EXPIRED로 변경
        assertEquals(TokenStatus.EXPIRED, token.getStatus());
    }

    @Test
    @DisplayName("restore 호출 시 만료 토큰을 ACTIVE 상태로 복구")
    void restore_token() {
        // given : EXPIRED 상태의 토큰
        Long userId = 6L;
        QueueToken token = new QueueToken(userId, LocalDateTime.now(clock));
        token.expire();
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(token));

        // when : restore 호출
        service.restore(userId);

        // then : 토큰 상태가 ACTIVE로 복구됨
        assertEquals(TokenStatus.ACTIVE, token.getStatus());
    }

    @Test
    @DisplayName("status 조회 시 Optional 반환")
    void status_returns_token() {
        // given : 저장된 토큰이 있는 userId
        Long userId = 7L;
        QueueToken token = new QueueToken(userId, LocalDateTime.now(clock));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(token));

        // when : 토큰 상태 조회
        Optional<QueueToken> result = service.status(userId);

        // then : 결과가 존재
        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("expireOverdueTokens는 토큰 만료 위임")
    void expire_delegates_to_manager() {
        // when : 스케줄러용 만료 처리 호출
        service.expireOverdueTokens();

        // then : TokenManager로 위임되었는지 검증
        verify(tokenManager).expireOverdueTokens(eq(5), any());
    }
}
