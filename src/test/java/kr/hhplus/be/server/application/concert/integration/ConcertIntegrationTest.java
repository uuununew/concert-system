package kr.hhplus.be.server.application.concert.integration;

import kr.hhplus.be.server.application.concert.ConcertService;
import kr.hhplus.be.server.application.concert.CreateConcertCommand;
import kr.hhplus.be.server.config.TestContainerConfig;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertStatus;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@Testcontainers
public class ConcertIntegrationTest extends TestContainerConfig {

    @Autowired
    private ConcertService concertService;

    @Test
    @DisplayName("콘서트를 등록할 수 있다.")
    void test_register_concert() {
        // given
        CreateConcertCommand command = new CreateConcertCommand(
                "BTS 콘서트", 1, ConcertStatus.READY, LocalDateTime.now().plusDays(3));

        // when
        Concert concert = concertService.registerConcert(command);

        // then
        assertThat(concert.getId()).isNotNull();
        assertThat(concert.getTitle()).isEqualTo("BTS 콘서트");
        assertThat(concert.getRound()).isEqualTo(1);
        assertThat(concert.getStatus()).isEqualTo(ConcertStatus.READY);
    }

    @Test
    @DisplayName("콘서트 ID로 공연을 조회할 수 있다.")
    void test_get_concert_by_id() {
        // given
        Concert saved = concertService.registerConcert(new CreateConcertCommand(
                "IU", 2, ConcertStatus.READY, LocalDateTime.now().plusDays(1)));

        // when
        Concert concert = concertService.getConcertById(saved.getId());

        // then
        assertThat(concert.getId()).isEqualTo(saved.getId());
        assertThat(concert.getTitle()).isEqualTo("IU");
    }

    @Test
    @DisplayName("전체 콘서트 목록을 조회할 수 있다.")
    void test_get_all_concerts() {
        // given
        concertService.registerConcert(new CreateConcertCommand("BTS1", 1, ConcertStatus.READY, LocalDateTime.now().plusDays(1)));
        concertService.registerConcert(new CreateConcertCommand("BTS2", 2, ConcertStatus.READY, LocalDateTime.now().plusDays(2)));

        // when
        List<Concert> concerts = concertService.getConcertList();

        // then
        assertThat(concerts).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("OPENED 상태의 콘서트만 조회할 수 있다")
    void test_get_opened_concerts() {
        // given
        Concert closed = concertService.registerConcert(new CreateConcertCommand("닫힌 콘서트", 1, ConcertStatus.CLOSED, LocalDateTime.now().plusDays(2)));
        Concert ready = concertService.registerConcert(new CreateConcertCommand("열린 콘서트", 2, ConcertStatus.READY, LocalDateTime.now().plusDays(2)));

        concertService.changeConcertStatus(ready.getId(), ConcertStatus.OPENED);

        // when
        List<Concert> openedList = concertService.getOpenedConcerts();

        // then
        assertThat(openedList).extracting(Concert::getId).contains(ready.getId());
        assertThat(openedList).extracting(Concert::getId).doesNotContain(closed.getId());
    }

    @Test
    @DisplayName("READY 상태가 아니면 콘서트를 OPEN할 수 없다")
    void open_concert_fail_if_not_ready() {
        // given
        Concert concert = concertService.registerConcert(new CreateConcertCommand(
                "샤이니 콘서트", 1, ConcertStatus.CLOSED, LocalDateTime.now().plusDays(1)));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> concertService.changeConcertStatus(concert.getId(), ConcertStatus.OPENED));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CONCERT_STATUS);
        assertThat(exception.getMessage()).contains("READY 상태여야 열 수 있습니다");
    }

    @Test
    @DisplayName("콘서트 상태를 변경할 수 있다")
    void test_change_concert_status() {
        // given
        Concert concert = concertService.registerConcert(new CreateConcertCommand(
                "BLACKPINK", 1, ConcertStatus.READY, LocalDateTime.now().plusDays(1)));

        // when
        concertService.changeConcertStatus(concert.getId(), ConcertStatus.OPENED);
        concertService.changeConcertStatus(concert.getId(), ConcertStatus.CLOSED);

        // then
        Concert updated = concertService.getConcertById(concert.getId());
        assertThat(updated.getStatus()).isEqualTo(ConcertStatus.CLOSED);
    }

    @Test
    @DisplayName("OPENED 상태가 아니면 콘서트를 CLOSE할 수 없다")
    void close_concert_fail_if_not_opened() {
        // given
        Concert concert = concertService.registerConcert(new CreateConcertCommand(
                "NewJeans 콘서트", 1, ConcertStatus.READY, LocalDateTime.now().plusDays(1)));

        // when //then
        CustomException exception = assertThrows(CustomException.class,
                () -> concertService.changeConcertStatus(concert.getId(), ConcertStatus.CLOSED));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CONCERT_STATUS);
        assertThat(exception.getMessage()).contains("OPENED 상태여야 종료할 수 있습니다");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 콘서트 조회 시 예외 발생")
    void test_get_concert_by_invalid_id() {
        // when // then
        CustomException exception = assertThrows(CustomException.class, () -> concertService.getConcertById(-1L));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(exception.getMessage()).contains("해당 ID의 콘서트를 찾을 수 없습니다.");
    }
}
