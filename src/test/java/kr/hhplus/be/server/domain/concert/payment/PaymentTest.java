package kr.hhplus.be.server.domain.concert.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentTest {
    @Test
    @DisplayName("결제를 생성한다")
    void create_payment_success() {
        // given
        Long userId = 1L;
        Long reservationId = 10L;
        BigDecimal amount = BigDecimal.valueOf(10000);
        LocalDateTime paidAt = LocalDateTime.now();

        // when
        Payment payment = Payment.create(userId, reservationId, amount, paidAt);

        // then
        assertThat(payment.getId()).isNull(); // 아직 저장되지 않았으므로 null
        assertThat(payment.getUserId()).isEqualTo(userId);
        assertThat(payment.getReservationId()).isEqualTo(reservationId);
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getPaidAt()).isEqualTo(paidAt);
    }
}
