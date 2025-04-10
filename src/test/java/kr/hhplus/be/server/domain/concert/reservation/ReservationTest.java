package kr.hhplus.be.server.domain.concert.reservation;

import kr.hhplus.be.server.application.concert.reservation.ReservationCommandService;
import kr.hhplus.be.server.domain.concert.reservation.Reservation;
import kr.hhplus.be.server.domain.concert.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.concert.reservation.ReservationStatus;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class ReservationTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationCommandService reservationCommandService;

    @Test
    @DisplayName("정상 예약 생성 시 RESERVED 상태로 생성된다")
    void create_success() {
        Reservation reservation = Reservation.create(1L, 1L, BigDecimal.valueOf(10000));

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservation.getPrice()).isEqualTo(BigDecimal.valueOf(10000));
    }

    @Test
    @DisplayName("RESERVED 상태의 예약은 CANCELED 상태로 변경 가능")
    void cancel_success() {
        Reservation reservation = Reservation.create(1L, 1L, BigDecimal.valueOf(10000));

        Reservation canceled = reservation.cancel();

        assertThat(canceled.getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("PAID 상태의 예약은 취소 불가")
    void cancel_fail_when_paid() {
        Reservation reservation = Reservation.create(1L, 1L, BigDecimal.valueOf(10000))
                .markPaid();

        assertThatThrownBy(reservation::cancel)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("결제 완료된 예약은 취소할 수 없습니다.");
    }

    @Test
    @DisplayName("RESERVED 상태의 예약은 PAID 상태로 변경 가능")
    void markPaid_success() {
        Reservation reservation = Reservation.create(1L, 1L, BigDecimal.valueOf(10000));

        Reservation paid = reservation.markPaid();

        assertThat(paid.getStatus()).isEqualTo(ReservationStatus.PAID);
        assertThat(paid.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("RESERVED 상태가 아니면 결제 완료로 변경할 수 없다")
    void markPaid_fail_when_not_reserved() {
        Reservation reservation = new Reservation(
                1L, 1L, 1L, BigDecimal.valueOf(10000),
                ReservationStatus.CANCELED, null,
                LocalDateTime.now(), LocalDateTime.now()
        );

        assertThatThrownBy(reservation::markPaid)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("RESERVED 상태일 때만 결제 가능합니다.");
    }
}
