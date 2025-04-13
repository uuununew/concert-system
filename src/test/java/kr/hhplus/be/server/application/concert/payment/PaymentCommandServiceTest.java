package kr.hhplus.be.server.application.concert.payment;

import kr.hhplus.be.server.application.cash.CashCommandService;
import kr.hhplus.be.server.domain.concert.payment.Payment;
import kr.hhplus.be.server.domain.concert.payment.PaymentStatus;
import kr.hhplus.be.server.domain.concert.reservation.Reservation;
import kr.hhplus.be.server.domain.concert.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.concert.payment.PaymentRepository;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentCommandServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CashCommandService cashCommandService;

    @InjectMocks
    private PaymentCommandService paymentCommandService;

    @Test
    @DisplayName("RESERVED 상태의 예약일 경우 결제가 정상적으로 이루어진다")
    void pay_success() {
        // given : 정상적인 예약이 존재하고 상태가 RESERVED인 경우
        Long reservationId = 1L;
        Long userId = 10L;
        BigDecimal amount = BigDecimal.valueOf(5000);
        CreatePaymentCommand command = new CreatePaymentCommand(userId, reservationId, amount);

        Reservation reserved = Reservation.create(userId, 100L, amount);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reserved));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when : 결제 요청
        Payment payment = paymentCommandService.pay(command);

        // then : 결제 정보가 저장되고, 예약 상태가 변경되며, 캐시 차감이 발생해야 함
        assertThat(payment.getUserId()).isEqualTo(userId);
        assertThat(payment.getAmount()).isEqualByComparingTo(amount);

        verify(cashCommandService).use(argThat(cmd ->
                cmd.getUserId().equals(userId) && cmd.getAmount().compareTo(amount) == 0
        ));
        verify(reservationRepository).save(any());
        verify(paymentRepository).save(any());
    }

    @Test
    @DisplayName("예약이 존재하지 않을 경우 예외가 발생한다")
    void pay_fail_when_reservation_not_found() {
        // given : : 존재하지 않는 예약 ID로 결제 요청
        Long reservationId = 99L;
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(10000);
        CreatePaymentCommand command = new CreatePaymentCommand(userId, reservationId, amount);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // when//then : 예외 발생
        assertThatThrownBy(() -> paymentCommandService.pay(command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("예약 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("예약 상태가 RESERVED가 아닐 경우 예외가 발생한다")
    void pay_fail_when_reservation_status_is_not_reserved() {
        // given : 예약 상태가 이미 PAID인 경우
        Long reservationId = 1L;
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(10000);
        CreatePaymentCommand command = new CreatePaymentCommand(userId, reservationId, amount);

        Reservation reserved = Reservation.create(userId, 10L, amount).pay(); // PAID 상태로 변경
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reserved));

        // when//then : 결제 불가 예외 발생
        assertThatThrownBy(() -> paymentCommandService.pay(command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("결제 가능한 상태가 아닙니다.");
    }

    @DisplayName("결제를 취소한다")
    @Test
    void cancel_payment_success() {
        // given : 정상적으로 결제된 상태의 결제 정보
        Long paymentId = 1L;
        Payment payment = new Payment(
                paymentId,
                1L,
                10L,
                PaymentStatus.PAID,
                BigDecimal.valueOf(10000),
                LocalDateTime.now()
        );

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when : 결제 취소 요청
        Payment result = paymentCommandService.cancel(paymentId);

        // then : 상태가 CANCELED이고 paidAt 필드가 null 처리되었는지 확인
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.CANCELED); // 상태 확인
        assertThat(result.getPaidAt()).isNull();
        verify(paymentRepository).save(any());
    }

    @DisplayName("이미 취소된 결제는 다시 취소할 수 없다")
    @Test
    void cancel_payment_fail_when_already_canceled() {
        // given : 이미 취소된 상태
        Long paymentId = 1L;
        Payment canceledPayment = new Payment(
                paymentId,
                1L,
                10L,
                PaymentStatus.CANCELED,                      //취소된 상태
                BigDecimal.valueOf(10000),
                null                                         //paidAt도 null
        );

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(canceledPayment));

        // when / then
        assertThatThrownBy(() -> paymentCommandService.cancel(paymentId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 취소된 결제입니다.");
    }

    @Test
    @DisplayName("결제 ID가 존재하지 않으면 예외 발생한다.")
    void cancel_payment_fail_when_not_found() {
        // given : 존재하지 않는 결제 ID로 취소 요청
        Long paymentId = 1L;

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> paymentCommandService.cancel(paymentId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("결제 정보를 찾을 수 없습니다.");
    }

}
