package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.payment.PaymentQueryService;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
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
        List<Payment> payments = List.of(
                new Payment(1L, userId, 10L, PaymentStatus.PAID, BigDecimal.valueOf(10000), LocalDateTime.now()),
                new Payment(2L, userId, 11L, PaymentStatus.PAID, BigDecimal.valueOf(15000), LocalDateTime.now())
        );

        when(paymentRepository.findByUserId(userId)).thenReturn(payments);

        // when
        List<Payment> result = paymentQueryService.findByUserId(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
        verify(paymentRepository, times(1)).findByUserId(userId);
    }

}
