package kr.hhplus.be.server.application.cash;

import kr.hhplus.be.server.domain.cash.CashHistoryRepository;
import kr.hhplus.be.server.domain.cash.UserCash;
import kr.hhplus.be.server.domain.cash.UserCashRepository;
import kr.hhplus.be.server.presentation.cash.CashResponse;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class CashServiceTest {

    @Test
    @DisplayName("존재하지 않는 유저일 경우 예외가 발생하고 이후 로직은 실행되지 않는다")
    void user_not_found_throw_and_not_execute_further() {
        // given
        Long userId = 99L;
        UserCashRepository mockUserCashRepository = mock(UserCashRepository.class);
        CashHistoryRepository mockCashHistoryRepository = mock(CashHistoryRepository.class);

        when(mockUserCashRepository.findByUserId(userId)).thenReturn(Optional.empty());

        CashService cashService = new CashService(mockUserCashRepository, mockCashHistoryRepository);

        // when// then
        assertThrows(CustomException.class, () ->
                cashService.use(new UseCashCommand(userId, BigDecimal.valueOf(1000))));

        //이후 로직(userCash.use or save)은 실행되면 안됨
        verify(mockUserCashRepository, never()).save(any());
    }

    @Test
    @DisplayName("유저의 캐시에 금액을 충전하면 잔액이 정상적으로 증가한다")
    void charge_increases_balance_success(){
        // given : 잔액이 1000원인 유저
        Long userId = 1L;
        UserCash user = new UserCash(userId, BigDecimal.valueOf(1000));
        UserCashRepository mockUserCashRepository = mock(UserCashRepository.class);
        CashHistoryRepository mockCashHistoryRepository = mock(CashHistoryRepository.class);

        when(mockUserCashRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        CashService service = new CashService(mockUserCashRepository, mockCashHistoryRepository);

        // when : 500원 충전하면
        ChargeCashCommand command = new ChargeCashCommand(userId, BigDecimal.valueOf(500));
        CashResponse response = service.charge(command);

        // then : 1500원이 됨
        assertEquals(BigDecimal.valueOf(1500), response.balance());
        verify(mockUserCashRepository).save(user);
        verify(mockCashHistoryRepository).save(any());
    }

    @Test
    @DisplayName("0원 충전 시 예외가 발생하고 save는 호출되지 않는다.")
    void charge_zero_fail_not_call_save() {
        // given
        Long userId = 1L;
        UserCash userCash = new UserCash(userId, BigDecimal.ZERO);
        UserCashRepository mockUserCashRepository = mock(UserCashRepository.class);
        CashHistoryRepository mockCashHistoryRepository = mock(CashHistoryRepository.class);

        when(mockUserCashRepository.findByUserId(userId)).thenReturn(Optional.of(userCash));

        CashService cashService = new CashService(mockUserCashRepository, mockCashHistoryRepository);

        // when//then
        assertThrows(CustomException.class, () ->
                cashService.charge(new ChargeCashCommand(userId, BigDecimal.ZERO)));

        verify(mockUserCashRepository, never()).save(any());
        verify(mockCashHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("최대 충전 한도까지는 충전이 가능하다.")
    void charge_up_to_max_limit_success() {
        // given : 잔액이 800,000원인 유저
        Long userId = 1L;
        UserCash userCash = new UserCash(userId, BigDecimal.valueOf(800_000));
        UserCashRepository mockUserCashRepository = mock(UserCashRepository.class);
        CashHistoryRepository mockCashHistoryRepository = mock(CashHistoryRepository.class);

        when(mockUserCashRepository.findByUserId(userId)).thenReturn(Optional.of(userCash));

        CashService cashService = new CashService(mockUserCashRepository, mockCashHistoryRepository);

        // when : 200,000 충전하면
        ChargeCashCommand command = new ChargeCashCommand(userId, BigDecimal.valueOf(200_000));
        CashResponse cashResponse = cashService.charge(command);

        // then : 잔액은 1,000,000원이 되어야 한다.
        assertEquals(BigDecimal.valueOf(1_000_000), cashResponse.balance());
        verify(mockUserCashRepository).save(userCash);
        verify(mockCashHistoryRepository).save(any());
    }

    @Test
    @DisplayName("최대 충전 금액을 초과하면 예외가 발생하고 save는 호출되지 않는다.")
    void charge_exceeds_max_fail_not_call_save() {
        // given : 잔액이 990,000인 유저
        Long userId = 1L;
        UserCash userCash = new UserCash(userId, BigDecimal.valueOf(999_000));
        UserCashRepository mockUserCashRepository = mock(UserCashRepository.class);
        CashHistoryRepository mockCashHistoryRepository = mock(CashHistoryRepository.class);

        when(mockUserCashRepository.findByUserId(userId)).thenReturn(Optional.of(userCash));

        CashService cashService = new CashService(mockUserCashRepository, mockCashHistoryRepository);

        // when // then : 2000원 충전하면 예외
        assertThrows(CustomException.class, () ->
                cashService.charge(new ChargeCashCommand(userId, BigDecimal.valueOf(2_000))));

        verify(mockUserCashRepository, never()).save(any());
        verify(mockCashHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("유저의 캐시를 사용하면 잔액이 감소한다.")
    void use_decreases_balance_success(){
        //given : 잔액이 1000원인 유저
        Long userId = 1L;
        UserCash userCash = new UserCash(userId, BigDecimal.valueOf(1000));
        UserCashRepository mockUserCashRepository = mock(UserCashRepository.class);
        CashHistoryRepository mockCashHistoryRepository = mock(CashHistoryRepository.class);
        when(mockUserCashRepository.findByUserId(userId)).thenReturn(Optional.of(userCash));

        CashService cashService = new CashService(mockUserCashRepository, mockCashHistoryRepository);


        //when : 300원 사용
        UseCashCommand command = new UseCashCommand(userId, BigDecimal.valueOf(300));
        CashResponse response = cashService.use(command);

        //then : 잔액은 700원이 되어야함
        assertEquals(BigDecimal.valueOf(700), response.balance());
        verify(mockUserCashRepository).save(userCash);
        verify(mockCashHistoryRepository).save(any());
    }

    @Test
    @DisplayName("잔액보다 많은 금액을 사용하면 예외가 발생하고 save는 호출되지 않는다.")
    void use_exceeds_balance_fail_not_call_save() {
        // given : 잔액이 500원인 유저
        Long userId = 2L;
        UserCash userCash = new UserCash(userId, BigDecimal.valueOf(500));
        UserCashRepository mockUserCashRepository = mock(UserCashRepository.class);
        CashHistoryRepository mockCashHistoryRepository = mock(CashHistoryRepository.class);

        when(mockUserCashRepository.findByUserId(userId)).thenReturn(Optional.of(userCash));

        CashService cashService = new CashService(mockUserCashRepository, mockCashHistoryRepository);

        // when // then : 1000원을 사용하면 예외
        assertThrows(CustomException.class, () ->
                cashService.use(new UseCashCommand(userId, BigDecimal.valueOf(1000))));

        verify(mockUserCashRepository, never()).save(any());
        verify(mockCashHistoryRepository, never()).save(any());
    }
}
