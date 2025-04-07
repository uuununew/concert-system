package kr.hhplus.be.server.domain.cash;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class CashServiceTest {

    @Test
    @DisplayName("유저의 캐시에 금액을 충전하면 잔액이 정상적으로 증가한다")
    void charge_increases_balance_success(){
        //given
        Long userId = 1L;
        UserCash userCash = new UserCash(userId, BigDecimal.valueOf(1000)); //잔액 1000원
        UserCashRepository mockRepository = mock(UserCashRepository.class);

        when(mockRepository.findByUserId(userId)).thenReturn(userCash);

        CashService cashService = new CashService(mockRepository); //테스트 대상 서비스 생성

        //when : 사용자에게 500원을 충전한다.
        BigDecimal updatedAmount = cashService.charge(userId, BigDecimal.valueOf(500));

        //then : 잔액은 1500원이 되어야한다.
        assertEquals(BigDecimal.valueOf(1500), updatedAmount);
        verify(mockRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("0원을 충전하면 예외가 발생한다.")
    void charge_zero_fail() {
        // given: 잔액이 0원인 사용자
        Long userId = 1L;
        UserCash userCash = new UserCash(userId, BigDecimal.ZERO);
        UserCashRepository mockRepository = mock(UserCashRepository.class);

        when(mockRepository.findByUserId(userId)).thenReturn(userCash);

        CashService service = new CashService(mockRepository);

        // when // then: 0원 충전 시 IllegalArgumentException 발생
        assertThrows(IllegalArgumentException.class, () ->
                service.charge(userId, BigDecimal.ZERO));
    }

    @Test
    @DisplayName("최대 충전 한도까지는 충전이 가능하다.")
    void charge_up_to_max_limit_success() {
        // given : 잔액이 800,000원인 유저
        Long userId = 1L;
        UserCash userCash = new UserCash(userId, BigDecimal.valueOf(800_000));

        UserCashRepository mockRepository = mock(UserCashRepository.class);
        when(mockRepository.findByUserId(userId)).thenReturn(userCash);

        CashService service = new CashService(mockRepository);

        // when : 200,000 충전하면
        BigDecimal result = service.charge(userId, BigDecimal.valueOf(200_000));

        // then : 잔액은 1,000,000원이 되어야 한다.
        assertEquals(BigDecimal.valueOf(1_000_000), result);
    }

    @Test
    @DisplayName("최대 충전 금액을 초과하면 예외가 발생한다.")
    void charge_exceeds_max_limit_fail() {
        // given: 현재 잔액이 999,000원인 사용자
        Long userId = 1L;
        UserCash userCash = new UserCash(userId, BigDecimal.valueOf(999_000));

        UserCashRepository mockRepository = mock(UserCashRepository.class);
        when(mockRepository.findByUserId(userId)).thenReturn(userCash);

        CashService service = new CashService(mockRepository);

        // when // then: 2,000원 충전 시 예외 발생 (한도 초과)
        assertThrows(IllegalArgumentException.class, () ->
                service.charge(userId, BigDecimal.valueOf(2_000)));
    }

}
