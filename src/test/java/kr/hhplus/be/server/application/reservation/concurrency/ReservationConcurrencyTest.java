package kr.hhplus.be.server.application.reservation.concurrency;

import kr.hhplus.be.server.application.concert.ConcertSeatCommandService;
import kr.hhplus.be.server.application.concert.ConcertService;
import kr.hhplus.be.server.application.concert.CreateConcertCommand;
import kr.hhplus.be.server.application.reservation.CreateReservationCommand;
import kr.hhplus.be.server.application.reservation.ReservationCommandService;
import kr.hhplus.be.server.config.TestContainerConfig;
import kr.hhplus.be.server.domain.concert.*;
import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.support.concurrency.ConcurrencyTestExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class ReservationConcurrencyTest extends TestContainerConfig {

    @Autowired
    private ConcertService concertService;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private ReservationCommandService reservationCommandService;

    private Long seatId;

    @BeforeEach
    void setUp(){
        //콘서트 생성
        Concert concert = concertService.registerConcert(new CreateConcertCommand(
            "콜드플레이", 1, ConcertStatus.OPENED, LocalDateTime.of(2025, 5, 1,  20, 0)));

        //좌석 생성
        ConcertSeat seat = ConcertSeat.of(
                concert, "1", "1층", "A", "R", BigDecimal.valueOf(100000));
        concertSeatRepository.save(seat);
        seatId = seat.getId();
    }

    @DisplayName("동시에 같은 좌석을 예약할 경우 하나만 성공해야 한다.")
    @Test
    void reserveSeat_concurrent_fail_on_duplicate() throws InterruptedException{
        //given : 10개의 스레드가 동시에 같은 좌석을 예약 시도
        int threadCount = 10;
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        //when
        ConcurrencyTestExecutor.run(threadCount, () -> {
            try{
                Long userId = Thread.currentThread().getId(); //유저 다르게 설정

                tokenRepository.save(new QueueToken(userId, LocalDateTime.now()));

                reservationCommandService.reserve(new CreateReservationCommand(userId, seatId, BigDecimal.valueOf(10000)));
            }catch (Throwable t){
                exceptions.add(t);
            }
        });

        //then : 성공한 요청은 오직 1개여야 함
        long successCount = threadCount - exceptions.size();
        assertThat(successCount).isEqualTo(1);
    }
}
