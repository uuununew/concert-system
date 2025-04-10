package kr.hhplus.be.server.domain.cash;

import kr.hhplus.be.server.application.cash.CashService;
import kr.hhplus.be.server.application.cash.ChargeCashCommand;
import kr.hhplus.be.server.presentation.cash.CashResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserCashTest {

    @Test
    @DisplayName("캐시를 충전하면 잔액이 증가한다.")
    void charge_increases_amount_success(){
        //given : 초기 금액이 0원인 사용자
        UserCash userCash = new UserCash(1L, BigDecimal.ZERO);

        //when : 1000원을 충전하면
        userCash.charge(BigDecimal.valueOf(1000));

        //then : 잔액은 1000원이 된다.
        assertEquals(BigDecimal.valueOf(1000), userCash.getAmount());
    }

    @Test
    @DisplayName("최대 충전 한도까지는 충전이 가능하다.")
    void charge_max_limit_success() {
        // given: 현재 잔액이 900,000원인 사용자
        UserCash userCash = new UserCash(1L, BigDecimal.valueOf(900_000));

        // when: 100,000원을 충전하면
        userCash.charge(BigDecimal.valueOf(100_000));

        // then: 잔액은 1,000,000원이 된다
        assertEquals(BigDecimal.valueOf(1_000_000), userCash.getAmount());
    }

    @Test
    @DisplayName("최대 충전 가능 금액을 초과하면 예외가 발생한다.")
    void charge_exceeds_max_limit_fail() {
        // given : 잔액이 999000원
        UserCash userCash = new UserCash(1L, BigDecimal.valueOf(999_000));

        // when // then : 999000 + 2000 = 1,001,000 예외 발생
        assertThrows(IllegalArgumentException.class, () ->
                userCash.charge(BigDecimal.valueOf(2000)));
    }

    @Test
    @DisplayName("캐시가 존재하지 않는 유저에게 충전 시 새 UserCash를 생성하고 저장한다.")
    void charge_creates_new_usercash_when_not_exist() {
        // given
        Long userId = 10L;
        UserCashRepository mockUserCashRepository = mock(UserCashRepository.class);
        CashHistoryRepository mockCashHistoryRepository = mock(CashHistoryRepository.class);

        when(mockUserCashRepository.findByUserId(userId)).thenReturn(Optional.empty());

        CashService cashService = new CashService(mockUserCashRepository, mockCashHistoryRepository);

        // when : 존재하지 않던 유저에게 캐시 충전
        CashResponse response = cashService.charge(new ChargeCashCommand(userId, BigDecimal.valueOf(1000)));

        // then
        assertEquals(BigDecimal.valueOf(1000), response.balance());
        verify(mockUserCashRepository).save(any(UserCash.class));
        verify(mockCashHistoryRepository).save(any());
    }

    @Test
    @DisplayName("잔액보다 많은 금액을 사용하면 예외가 발생한다.")
    void use_more_than_amount_fail(){
        //given : 잔액이 1000원
        UserCash userCash = new UserCash(1L, BigDecimal.valueOf(1000));

        //when//then : 2000원 사용하려고 하면 예외 발생
        assertThrows(IllegalArgumentException.class, () ->
                userCash.use(BigDecimal.valueOf(2000)));
    }

    @Test
    @DisplayName("캐시를 사용하면 잔액이 감소한다")
    void use_decreases_amount_success() {
        // given: 잔액이 1,000원인 사용자
        UserCash userCash = new UserCash(1L, BigDecimal.valueOf(1000));

        // when: 500원 사용
        userCash.use(BigDecimal.valueOf(500));

        // then: 잔액은 500원이 된다
        assertEquals(BigDecimal.valueOf(500), userCash.getAmount());
    }

    @Test
    @DisplayName("잔액과 동일한 금액을 사용하는 것은 가능하다.")
    void use_equals_balance_success(){
        //given : 잔액이 1000원인 유저
        UserCash userCash = new UserCash(1L, BigDecimal.valueOf(1000));

        //when : 1000원 사용
        userCash.use(BigDecimal.valueOf(1000));

        //then : 잔액은 0원
        assertEquals(BigDecimal.ZERO, userCash.getAmount());
    }

    @Test
    @DisplayName("0원을 사용하면 예외가 발생한다.")
    void use_zero_fail() {
        // given : 잔액이 1000원인 사용자
        UserCash userCash = new UserCash(1L, BigDecimal.valueOf(1000));

        // when//then : 0원 사용 시 예외
        assertThrows(IllegalArgumentException.class, () ->
                userCash.use(BigDecimal.ZERO));
    }
}
