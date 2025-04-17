package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.payment.PaymentQueryService;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.ConcertStatus;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.user.User;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentQueryServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentQueryService paymentQueryService;

    @Test
    @DisplayName("사용자 ID로 결제 내역을 조회한다")
    void find_by_user_id_success() {
        // given
        Long userId = 1L;
        BigDecimal amount1 = BigDecimal.valueOf(10000);
        BigDecimal amount2 = BigDecimal.valueOf(15000);

        User user = new User(userId);
        Concert concert = Concert.withStatus(ConcertStatus.READY);
        ConcertSeat seat1 = ConcertSeat.withAll(1L, concert, "A1", "1층", "A", "VIP", amount1, SeatStatus.AVAILABLE, LocalDateTime.now());
        ConcertSeat seat2 = ConcertSeat.withAll(2L, concert, "A2", "2층", "B", "VIP", amount2, SeatStatus.AVAILABLE, LocalDateTime.now());

        Reservation reservation1 = Reservation.create(user, seat1, amount1);
        Reservation reservation2 = Reservation.create(user, seat2, amount2);

        Payment payment1 = Payment.withAll(1L, reservation1, PaymentStatus.PAID, amount1, LocalDateTime.now());
        Payment payment2 = Payment.withAll(2L, reservation2, PaymentStatus.PAID, amount2, LocalDateTime.now());

        when(paymentRepository.findByUserId(userId)).thenReturn(List.of(payment1, payment2));

        // when
        List<Payment> result = paymentQueryService.findByUserId(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReservation().getUserId()).isEqualTo(userId);
        verify(paymentRepository, times(1)).findByUserId(userId);
    }
}
