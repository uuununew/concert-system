package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentTest {

    @Test
    @DisplayName("결제를 생성하면 READY 상태이고 결제 시간은 없다")
    void create_payment_success() {
        // given
        Long userId = 1L;
        Long reservationId = 10L;
        BigDecimal amount = BigDecimal.valueOf(10000);

        // when
        Payment payment = Payment.create(userId, reservationId, amount);

        // then
        assertThat(payment.getId()).isNull(); // 아직 저장되지 않았으므로 null
        assertThat(payment.getUserId()).isEqualTo(userId);
        assertThat(payment.getReservationId()).isEqualTo(reservationId);
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(payment.getPaidAt()).isNull(); 
    }

    @Test
    @DisplayName("READY 상태의 결제를 pay() 하면 PAID 상태로 전이되고 결제 시간이 기록된다")
    void pay_success() {
        // given
        Payment payment = Payment.create(1L, 10L, BigDecimal.valueOf(10000));

        // when
        payment.pay();

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("READY 상태가 아닌 결제는 pay() 할 수 없다")
    void pay_fail_when_not_ready() {
        // given
        Payment payment = Payment.withAll(1L, 1L, 10L, PaymentStatus.FAILED, BigDecimal.valueOf(10000), null);

        // when // then
        assertThatThrownBy(payment::pay)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("READY 상태일 때만 결제가 가능합니다");
    }

    @Test
    @DisplayName("PAID 상태의 결제는 cancel() 하면 CANCELED 상태로 전이된다")
    void cancel_success() {
        // given
        Payment payment = Payment.withAll(
                1L, 1L, 10L, PaymentStatus.PAID, BigDecimal.valueOf(10000), LocalDateTime.now());

        // when
        payment.cancel(); // 반환값 없이 상태만 바뀜

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
        assertThat(payment.getPaidAt()).isNull();
    }

    @Test
    @DisplayName("PAID 상태가 아닌 결제를 cancel() 하면 예외가 발생한다")
    void cancel_fail_when_not_paid() {
        // given
        Payment payment = Payment.create(1L, 10L, BigDecimal.valueOf(10000)); // status: READY

        // when // then
        assertThatThrownBy(payment::cancel)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("결제된 건만 취소할 수 있습니다");
    }

    @Test
    @DisplayName("READY 상태의 결제를 fail() 하면 FAILED 상태로 전이된다")
    void fail_success() {
        // given
        Payment payment = Payment.create(1L, 10L, BigDecimal.valueOf(10000));

        // when
        payment.fail();

        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("READY 상태가 아닌 결제를 fail() 하면 예외가 발생한다")
    void fail_fail_when_not_ready() {
        // given
        Payment payment = Payment.withAll(1L, 1L, 10L, PaymentStatus.PAID, BigDecimal.valueOf(10000), LocalDateTime.now());

        // when // then
        assertThatThrownBy(payment::fail)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("결제가 실패 처리될 수 없는 상태입니다");
    }
}
