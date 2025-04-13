package kr.hhplus.be.server.application.concert.reservation;

import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import kr.hhplus.be.server.domain.concert.reservation.Reservation;
import kr.hhplus.be.server.domain.concert.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.concert.reservation.ReservationScheduleService;
import kr.hhplus.be.server.domain.concert.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReservationSchedulerServiceTest {
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationScheduleService reservationScheduleService;

    @Test
    @DisplayName("미결제 예약을 취소 처리한다")
    void cancel_unpaid_reservations_success() {
        // given
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(10);
        BigDecimal price1 = BigDecimal.valueOf(10000);

        User user1 = new User(1L);

        ConcertSeat seat1 = ConcertSeat.withAll(10L, 100L, "A1", "1층", "A", "VIP", price1, SeatStatus.AVAILABLE, LocalDateTime.now());

        Reservation reservation1 = Reservation.create(user1, seat1, price1);

        when(reservationRepository.findAllByStatusAndCreatedAtBefore(ReservationStatus.RESERVED, cutoffTime))
                .thenReturn(List.of(reservation1));

        // when
        reservationScheduleService.cancelReservationsBefore(cutoffTime);

        // then
        verify(reservationRepository, times(1)).save(argThat(res ->
                res.getUserId().equals(reservation1.getUserId()) &&
                        res.getConcertSeatId().equals(reservation1.getConcertSeatId()) &&
                        res.getStatus() == ReservationStatus.CANCELED
        ));
    }
}
