package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.*;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConcertSeatCommandServiceTest {

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private ConcertSeatRepository concertSeatRepository;

    @InjectMocks
    private ConcertSeatCommandService commandService;

    @Test
    @DisplayName("좌석을 정상 등록한다")
    void register_seat_success() {
        // given
        Long concertId = 1L;
        CreateConcertSeatCommand command = new CreateConcertSeatCommand(
                concertId, "A1", "VIP", "1", "R", BigDecimal.valueOf(10000)
        );

        Concert concert = new Concert("블핑 콘서트", 1, ConcertStatus.OPENED, LocalDateTime.now().plusDays(1));
        when(concertRepository.findById(concertId)).thenReturn(Optional.of(concert));
        when(concertSeatRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ConcertSeat seat = commandService.registerSeat(command);

        // then
        assertThat(seat.getSeatNumber()).isEqualTo("A1");
        verify(concertSeatRepository).save(any());
    }

    @Test
    @DisplayName("좌석 등록 시 콘서트가 없으면 예외 발생")
    void register_seat_fail_when_concert_not_found() {
        // given
        Long concertId = 99L;
        CreateConcertSeatCommand command = new CreateConcertSeatCommand(
                concertId, "A1", "VIP", "1", "R", BigDecimal.valueOf(10000)
        );

        when(concertRepository.findById(concertId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> commandService.registerSeat(command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("해당 ID의 콘서트가 존재하지 않습니다.");
    }
}
