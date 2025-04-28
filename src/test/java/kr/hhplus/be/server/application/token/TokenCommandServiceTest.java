package kr.hhplus.be.server.application.token;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.config.TestContainerConfig;
import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenManager;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenCommandServiceTest{

    private TokenCommandService tokenCommandService;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private TokenManager tokenManager;

    @Mock
    private Clock clock;


    @BeforeEach
    void setUp() {
        tokenRepository = mock(TokenRepository.class);
        tokenManager = mock(TokenManager.class);
        clock = Clock.fixed(Instant.parse("2025-04-11T12:00:00Z"), ZoneOffset.UTC);

        tokenCommandService = new TokenCommandService(tokenRepository, tokenManager, clock, 5);
    }

    @Test
    @DisplayName("새 토큰이 없으면 발급 후 저장한다")
    void issue_new_token() {
        // given : 저장된 토큰이 없는 userId 설정
        Long userId = 1L;
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(tokenRepository.enqueue(eq(userId), eq(clock))).thenAnswer(inv -> new QueueToken(userId, LocalDateTime.now(clock)));

        // when : 토큰 발급 호출
        QueueToken token = tokenCommandService.issue(userId);

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
        CustomException ex = assertThrows(CustomException.class, () -> tokenCommandService.activate(userId));
        assertEquals(ErrorCode.TOKEN_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("만료된 토큰이 존재할 경우 새 토큰을 발급한다")
    void issue_expired_token_creates_new() {
        // given
        Long userId = 8L;
        QueueToken expiredToken = new QueueToken(userId, LocalDateTime.now(clock).minusMinutes(10));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(expiredToken));
        when(tokenRepository.enqueue(eq(userId), eq(clock))).thenAnswer(inv -> new QueueToken(userId, LocalDateTime.now(clock)));

        // when
        QueueToken newToken = tokenCommandService.issue(userId);

        // then
        assertNotSame(expiredToken, newToken); //기존 만료된 토큰과 다른 토큰
        assertEquals(TokenStatus.WAITING, newToken.getStatus());
    }

    @Test
    @DisplayName("이미 토큰이 있으면 기존 것을 반환한다")
    void issue_existing_token() {
        // given : 이미 발급된 토큰이 존재하는 userId
        Long userId = 2L;
        QueueToken existing = new QueueToken(userId, LocalDateTime.now(clock));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        // when : 토큰 발급 호출
        QueueToken result = tokenCommandService.issue(userId);

        // then : 기존 토큰을 그대로 반환
        assertSame(existing, result);
    }

    @Test
    @DisplayName("토큰이 유효하고 FIFO 순서일 때 활성화된다")
    void activate_valid_token() {
        // given : 아직 만료되지 않은 토큰
        Long userId = 3L;
        QueueToken token = new QueueToken(userId, LocalDateTime.now(clock).minusMinutes(2));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(token));
        when(tokenRepository.findAllByStatusOrderByIssuedAt(TokenStatus.WAITING)).thenReturn(List.of(token));

        // when : 토큰 활성화 시도
        tokenCommandService.activate(userId);

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
        CustomException ex = assertThrows(CustomException.class, () -> tokenCommandService.activate(userId));
        assertEquals(ErrorCode.TOKEN_NOT_FOUND, ex.getErrorCode());
        assertEquals(TokenStatus.EXPIRED, token.getStatus());
    }

    @Test
    @DisplayName("FIFO 순서가 아니면 예외가 발생한다")
    void activate_invalid_order_throws() {
        Long userId = 5L;
        QueueToken token = new QueueToken(userId, LocalDateTime.now(clock).minusMinutes(1));
        QueueToken firstInQueue = new QueueToken(999L, LocalDateTime.now(clock).minusMinutes(2));

        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(token));
        when(tokenRepository.findAllByStatusOrderByIssuedAt(TokenStatus.WAITING)).thenReturn(List.of(firstInQueue, token));

        CustomException ex = assertThrows(CustomException.class, () -> tokenCommandService.activate(userId));
        assertEquals(ErrorCode.INVALID_TOKEN_ORDER, ex.getErrorCode());
        assertEquals(TokenStatus.WAITING, token.getStatus());
    }

    @Test
    @DisplayName("complete 호출 시 토큰은 EXPIRED 상태가 된다")
    void complete_token() {
        // given : 예약 완료 처리할 ACTIVE 토큰
        Long userId = 5L;
        QueueToken token = new QueueToken(userId, LocalDateTime.now(clock));
        when(tokenRepository.findByUserId(userId)).thenReturn(Optional.of(token));

        // when : complete 호출
        tokenCommandService.complete(userId);

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
        tokenCommandService.restore(userId);

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
        Optional<QueueToken> result = tokenCommandService.status(userId);

        // then : 결과가 존재
        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("expireOverdueTokens는 토큰 만료 위임")
    void expire_delegates_to_manager() {
        // when : 스케줄러용 만료 처리 호출
        tokenCommandService.expireOverdueTokens();

        // then : TokenManager로 위임되었는지 검증
        verify(tokenManager).expireOverdueTokens(eq(5), any());
    }

    @DisplayName("대기 중인 토큰이 있을 경우 활성화 시도")
    @Test
    void activateEligibleTokens_should_call_manager() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Clock fixedClock = Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        TokenRepository mockRepo = mock(TokenRepository.class);
        TokenManager mockManager = mock(TokenManager.class);

        TokenCommandService service = new TokenCommandService(mockRepo, mockManager, fixedClock, 15);

        // when
        service.activateEligibleTokens();

        // then
        verify(mockManager).activateTokens(now);
    }

    @Test
    @DisplayName("발급 후 expireOverdueTokens 호출 시 만료되어야 한다")
    void expire_tokens_after_time() {
        // when
        tokenCommandService.expireOverdueTokens();

        // then
        verify(tokenManager).expireOverdueTokens(eq(5), any());
    }

    @Test
    @DisplayName("발급 후 activateEligibleTokens 호출 시 가장 오래된 토큰이 활성화된다")
    void activate_earliest_waiting_token() {
        // given
        LocalDateTime now = LocalDateTime.now(clock);
        Clock fixedClock = Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        TokenRepository mockRepo = mock(TokenRepository.class);
        TokenManager mockManager = mock(TokenManager.class);

        TokenCommandService service = new TokenCommandService(mockRepo, mockManager, fixedClock, 15);

        // when
        service.activateEligibleTokens();

        // then
        verify(mockManager).activateTokens(now);
    }
}
