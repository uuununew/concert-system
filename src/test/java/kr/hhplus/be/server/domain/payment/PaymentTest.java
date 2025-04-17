package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.ConcertStatus;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import kr.hhplus.be.server.domain.user.User;
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
        BigDecimal amount = BigDecimal.valueOf(10000);

        Concert concert = Concert.withStatus(ConcertStatus.READY);
        ConcertSeat seat = ConcertSeat.withAll(1L, concert, "A1", "1층", "A", "VIP", amount, SeatStatus.AVAILABLE, LocalDateTime.now());
        Reservation reservation = Reservation.create(new User(userId), seat, amount);


        // when
        Payment payment = Payment.create(reservation, amount);

        // then
        assertThat(payment.getId()).isNull();
        assertThat(payment.getReservation().getUserId()).isEqualTo(userId);
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(payment.getPaidAt()).isNull();
    }

    @Test
    @DisplayName("READY 상태의 결제를 pay() 하면 PAID 상태로 전이되고 결제 시간이 기록된다")
    void pay_success() {
        // given
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(10000);

        Concert concert = Concert.withStatus(ConcertStatus.READY);
        ConcertSeat seat = ConcertSeat.withAll(1L, concert, "A1", "1층", "A", "VIP", amount, SeatStatus.AVAILABLE, LocalDateTime.now());
        Reservation reservation = Reservation.create(new User(userId), seat, amount);

        Payment payment = Payment.create(reservation, amount);
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
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(10000);

        Concert concert = Concert.withStatus(ConcertStatus.READY);
        ConcertSeat seat = ConcertSeat.withAll(2L, concert, "A2", "2층", "B", "R", amount, SeatStatus.AVAILABLE, LocalDateTime.now());
        Reservation reservation = Reservation.create(new User(userId), seat, amount);

        //when
        Payment payment = Payment.withAll(1L, reservation, PaymentStatus.FAILED, amount, null);

         // then
        assertThatThrownBy(payment::pay)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("READY 상태일 때만 결제가 가능합니다");
    }

    @Test
    @DisplayName("PAID 상태의 결제는 cancel() 하면 CANCELED 상태로 전이된다")
    void cancel_success() {
        // given
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(10000);

        Concert concert = Concert.withStatus(ConcertStatus.READY);
        ConcertSeat seat = ConcertSeat.withAll(3L, concert, "A3", "3층", "C", "R", amount, SeatStatus.AVAILABLE, LocalDateTime.now());
        Reservation reservation = Reservation.create(new User(userId), seat, amount);

        Payment payment = Payment.withAll(1L, reservation, PaymentStatus.PAID, amount, LocalDateTime.now());

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
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(10000);

        Concert concert = Concert.withStatus(ConcertStatus.READY);
        ConcertSeat seat = ConcertSeat.withAll(4L, concert, "A4", "4층", "D", "S", amount, SeatStatus.AVAILABLE, LocalDateTime.now());
        Reservation reservation = Reservation.create(new User(userId), seat, amount);

        //when
        Payment payment = Payment.create(reservation, amount);

        // then
        assertThatThrownBy(payment::cancel)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("결제된 건만 취소할 수 있습니다");
    }

    @Test
    @DisplayName("READY 상태의 결제를 fail() 하면 FAILED 상태로 전이된다")
    void fail_success() {
        // given
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(10000);

        Concert concert = Concert.withStatus(ConcertStatus.READY);
        ConcertSeat seat = ConcertSeat.withAll(5L, concert, "A5", "5층", "E", "S", amount, SeatStatus.AVAILABLE, LocalDateTime.now());
        Reservation reservation = Reservation.create(new User(userId), seat, amount);

        Payment payment = Payment.create(reservation, amount);

        // when
        payment.fail();

        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("READY 상태가 아닌 결제를 fail() 하면 예외가 발생한다")
    void fail_fail_when_not_ready() {
        //given
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(10000);

        Concert concert = Concert.withStatus(ConcertStatus.READY);
        ConcertSeat seat = ConcertSeat.withAll(6L, concert, "A6", "6층", "F", "VIP", amount, SeatStatus.AVAILABLE, LocalDateTime.now());
        Reservation reservation = Reservation.create(new User(userId), seat, amount);

        //when
        Payment payment = Payment.withAll(1L, reservation, PaymentStatus.PAID, amount, LocalDateTime.now());

        // then
        assertThatThrownBy(payment::fail)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("결제가 실패 처리될 수 없는 상태입니다");
    }
}
