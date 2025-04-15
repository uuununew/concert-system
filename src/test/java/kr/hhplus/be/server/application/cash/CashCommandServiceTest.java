package kr.hhplus.be.server.application.cash;

import kr.hhplus.be.server.domain.cash.CashHistoryRepository;
import kr.hhplus.be.server.domain.cash.UserCash;
import kr.hhplus.be.server.domain.cash.UserCashRepository;
import org.junit.jupiter.api.BeforeEach;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
* 캐시 차감 단위테스트
 */
public class CashCommandServiceTest {

    private UserCashRepository userCashRepository;
    private CashCommandService service;
    private CashHistoryRepository cashHistoryRepository;

    @BeforeEach
    void setUp() {
        userCashRepository = mock(UserCashRepository.class);
        cashHistoryRepository = mock(CashHistoryRepository.class);
        service = new CashCommandService(userCashRepository, cashHistoryRepository);
    }

    @Test
    @DisplayName("잔액이 충분하면 정상 차감된다")
    void use_success() {
        // given
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(3000);
        UserCash userCash = new UserCash(userId, BigDecimal.valueOf(10000));
        when(userCashRepository.findByUserId(userId)).thenReturn(Optional.of(userCash));

        // when
        service.use(new UseCashCommand(userId, amount));

        // then
        assertEquals(BigDecimal.valueOf(7000), userCash.getAmount());
        verify(userCashRepository).save(userCash);
    }

    @Test
    @DisplayName("존재하지 않는 유저 ID는 예외 발생")
    void use_user_not_found() {
        // given
        Long userId = 2L;
        when(userCashRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class, () -> service.use(new UseCashCommand(userId, BigDecimal.valueOf(1000))));
    }
}
