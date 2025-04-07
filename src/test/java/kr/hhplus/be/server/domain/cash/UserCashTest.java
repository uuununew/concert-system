package kr.hhplus.be.server.domain.cash;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class UserCashTest {

    @Test
    @DisplayName("캐시를 충전하면 잔액이 증가한다.")
    void charge_increases_amount(){
        //given : 초기 금액이 0원인 사용자
        UserCash userCash = new UserCash(1L, BigDecimal.ZERO);

        //when : 1000원을 충전하면
        userCash.charge(BigDecimal.valueOf(1000));

        //then : 잔액은 1000원이 된다.
        assertEquals(BigDecimal.valueOf(1000), userCash.getAmount());
    }

    @Test
    @DisplayName("음수 금액 충전 시 예외가 발생한다.")
    void charge_negative(){
        //given : 잔액이 0원인 사용자
        UserCash userCash = new UserCash(1L, BigDecimal.ZERO);

        // when//then: -500원 충전 시 IllegalArgumentException 발생
        assertThrows(IllegalArgumentException.class, () ->
                userCash.charge(BigDecimal.valueOf(-500)));
    }

    @Test
    @DisplayName("0원을 충전하면 예외가 발생한다.")
    void charge_zero(){
        //given : 잔액이 0원인 사용자
        UserCash userCash = new UserCash(1L, BigDecimal.ZERO);

        //when//then : 0원 충전 시 예외 발생
        assertThrows(IllegalArgumentException.class, () ->
                userCash.charge(BigDecimal.ZERO));

    }

    @Test
    @DisplayName("최대 충전 한도까지는 충전이 가능하다.")
    void charge_max_limit() {
        // given: 현재 잔액이 900,000원인 사용자
        UserCash userCash = new UserCash(1L, BigDecimal.valueOf(900_000));

        // when: 100,000원을 충전하면
        userCash.charge(BigDecimal.valueOf(100_000));

        // then: 잔액은 1,000,000원이 된다
        assertEquals(BigDecimal.valueOf(1_000_000), userCash.getAmount());
    }

    @Test
    @DisplayName("최대 충전 가능 금액을 초과하면 예외가 발생한다.")
    void charge_exceeds_max_limit_throws_exception() {
        // given : 잔액이 999000원
        UserCash userCash = new UserCash(1L, BigDecimal.valueOf(999_000));

        // when // then : 999000 + 2000 = 1,001,000 예외 발생
        assertThrows(IllegalArgumentException.class, () ->
                userCash.charge(BigDecimal.valueOf(2000)));
    }

    @Test
    @DisplayName("잔액보다 많은 금액을 사용하면 예외가 발생한다.")
    void use_more_than_amount(){
        //given : 잔액이 1000원
        UserCash userCash = new UserCash(1L, BigDecimal.valueOf(1000));

        //when//then : 2000원 사용하려고 하면 예외 발생
        assertThrows(IllegalArgumentException.class, () ->
                userCash.use(BigDecimal.valueOf(2000)));
    }

    @Test
    @DisplayName("캐시를 사용하면 잔액이 감소한다")
    void use_decreases_amount() {
        // given: 잔액이 1,000원인 사용자
        UserCash userCash = new UserCash(1L, BigDecimal.valueOf(1000));

        // when: 500원 사용
        userCash.use(BigDecimal.valueOf(500));

        // then: 잔액은 500원이 된다
        assertEquals(BigDecimal.valueOf(500), userCash.getAmount());
    }

    @Test
    @DisplayName("0원을 사용하면 예외가 발생한다.")
    void use_zero() {
        // given: 잔액이 1000원인 사용자
        UserCash userCash = new UserCash(1L, BigDecimal.valueOf(1000));

        // when // then: 0원 사용 시 IllegalArgumentException 발생
        assertThrows(IllegalArgumentException.class, () ->
                userCash.use(BigDecimal.ZERO));
    }
}
