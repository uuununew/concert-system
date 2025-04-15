package kr.hhplus.be.server.domain.concert.reservation;

import kr.hhplus.be.server.application.concert.reservation.ReservationCommandService;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.ConcertStatus;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import kr.hhplus.be.server.domain.concert.reservation.Reservation;
import kr.hhplus.be.server.domain.concert.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.concert.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.user.User;
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
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ReservationTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationCommandService reservationCommandService;

    private Concert dummyConcert() {
        return Concert.withStatus(ConcertStatus.READY); // 또는 필요 시 withAll(...) 만들어도 OK
    }

    @Test
    @DisplayName("정상 예약 생성 시 RESERVED 상태로 생성된다")
    void create_success() {
        User user = new User(1L);
        Concert concert = dummyConcert();
        ConcertSeat seat = ConcertSeat.withAll(
                10L, concert, "A1", "1층", "A", "VIP",
                BigDecimal.valueOf(10000), SeatStatus.AVAILABLE, LocalDateTime.now()
        );
        BigDecimal amount = BigDecimal.valueOf(10000);

        Reservation reservation = Reservation.create(user, seat, amount);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservation.getPrice()).isEqualTo(amount);
        assertThat(reservation.getUserId()).isEqualTo(user.getId());
        assertThat(reservation.getConcertSeat().getId()).isEqualTo(seat.getId());
    }

    @Test
    @DisplayName("RESERVED 상태의 예약은 CANCELED 상태로 변경 가능")
    void cancel_success() {
        User user = new User(1L);
        Concert concert = dummyConcert();
        ConcertSeat seat = ConcertSeat.withAll(
                10L, concert, "A1", "1층", "A", "VIP",
                BigDecimal.valueOf(10000), SeatStatus.AVAILABLE, LocalDateTime.now()
        );
        BigDecimal amount = BigDecimal.valueOf(10000);

        Reservation reservation = Reservation.create(user, seat, amount);

        Reservation canceled = reservation.cancel();

        assertThat(canceled.getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("PAID 상태의 예약은 취소 불가")
    void cancel_fail_when_paid() {
        User user = new User(1L);
        Concert concert = dummyConcert();
        ConcertSeat seat = ConcertSeat.withAll(
                10L, concert, "A1", "1층", "A", "VIP",
                BigDecimal.valueOf(10000), SeatStatus.AVAILABLE, LocalDateTime.now()
        );
        BigDecimal amount = BigDecimal.valueOf(10000);

        Reservation reservation = Reservation.create(user, seat, amount).pay();

        assertThatThrownBy(reservation::cancel)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("결제 완료된 예약은 취소할 수 없습니다.");
    }

    @Test
    @DisplayName("RESERVED 상태의 예약은 PAID 상태로 변경 가능")
    void markPaid_success() {
        User user = new User(1L);
        Concert concert = dummyConcert();
        ConcertSeat seat = ConcertSeat.withAll(
                10L, concert, "A1", "1층", "A", "VIP",
                BigDecimal.valueOf(10000), SeatStatus.AVAILABLE, LocalDateTime.now()
        );
        BigDecimal amount = BigDecimal.valueOf(10000);

        Reservation reservation = Reservation.create(user, seat, amount);

        Reservation paid = reservation.pay();

        assertThat(paid.getStatus()).isEqualTo(ReservationStatus.PAID);
        assertThat(paid.getPaidAt()).isNotNull();
    }


    @Test
    @DisplayName("RESERVED 상태가 아니면 결제 완료로 변경할 수 없다")
    void markPaid_fail_when_not_reserved() {
        ConcertSeat seat = mock(ConcertSeat.class); // seat은 실제 구현 필요 없음
        Reservation reservation = new Reservation(
                1L, 1L,seat,
                BigDecimal.valueOf(10000), ReservationStatus.CANCELED, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());

        assertThatThrownBy(reservation::pay)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("RESERVED 상태일 때만 결제 가능합니다.");
    }
}
