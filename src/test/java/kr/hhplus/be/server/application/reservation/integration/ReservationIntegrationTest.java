package kr.hhplus.be.server.application.reservation.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import kr.hhplus.be.server.application.reservation.CreateReservationCommand;
import kr.hhplus.be.server.application.reservation.ReservationCommandService;
import kr.hhplus.be.server.application.reservation.event.ReservationInfoSender;
import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.config.TestContainerConfig;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import kr.hhplus.be.server.domain.concert.*;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@EnableAsync
@SpringBootTest
@Transactional
@Testcontainers
@AutoConfigureMockMvc
/*@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true"
})*/
public class ReservationIntegrationTest extends TestContainerConfig {

    @Autowired
    private ReservationCommandService reservationCommandService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private TokenCommandService tokenCommandService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestReservationInfoSender reservationInfoSender;

    private Long concertSeatId;
    private String tokenId;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        // 콘서트 생성
        Concert concert = concertRepository.save(
                Concert.create("테스트 콘서트", 1, ConcertStatus.READY, LocalDateTime.now().plusDays(1)));
        // 좌석 생성
        ConcertSeat seat = concertSeatRepository.save(
                ConcertSeat.of(concert, "A1", "1층", "A", "VIP", BigDecimal.valueOf(10000)));
        concertSeatId = seat.getId();

        // 토큰 발급 및 활성화
        tokenId = tokenCommandService.issue(userId);
        tokenCommandService.activateEligibleTokens(1000);
    }

    @Test
    @DisplayName("좌석을 정상적으로 예약할 수 있다")
    void reserve_success() {
        // given
        CreateReservationCommand command = new CreateReservationCommand(
                tokenId, userId, concertSeatId, BigDecimal.valueOf(10000));

        // when
        Reservation reservation = reservationCommandService.reserve(command);

        // then
        assertThat(reservation).isNotNull();
        assertThat(reservation.getUserId()).isEqualTo(1L);
        assertThat(reservation.getConcertSeat().getId()).isEqualTo(concertSeatId);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    @DisplayName("예약 ID로 예약 정보를 조회할 수 있다")
    void find_reservation_by_id() {
        // given
        CreateReservationCommand command = new CreateReservationCommand(tokenId, userId, concertSeatId, BigDecimal.valueOf(10000));
        Reservation reservation = reservationCommandService.reserve(command);

        // when
        Reservation found = reservationRepository.findById(reservation.getId())
                .orElseThrow();

        // then
        assertThat(found.getUserId()).isEqualTo(1L);
        assertThat(found.getConcertSeat().getId()).isEqualTo(concertSeatId);
    }

    @Test
    @DisplayName("이미 예약된 좌석은 예약할 수 없다")
    void reserve_fail_if_already_reserved() {
        // given
        CreateReservationCommand command = new CreateReservationCommand(tokenId, userId, concertSeatId, BigDecimal.valueOf(10000));
        reservationCommandService.reserve(command);

        // 재시도용 토큰
        String newTokenId = tokenCommandService.issue(userId);
        tokenCommandService.activateEligibleTokens(1000);

        CreateReservationCommand secondCommand = new CreateReservationCommand(
                newTokenId, userId, concertSeatId, BigDecimal.valueOf(10000));

        // when // then
        assertThatThrownBy(() -> reservationCommandService.reserve(secondCommand))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("예약할 수 없는 좌석입니다");
    }

    @Test
    @DisplayName("존재하지 않는 좌석 ID로 예약하면 예외가 발생한다")
    void reserve_fail_if_seat_not_found() {
        // given
        CreateReservationCommand command = new CreateReservationCommand(
                tokenId, userId, 9999L, BigDecimal.valueOf(10000));

        // when //then
        assertThatThrownBy(() -> reservationCommandService.reserve(command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("해당 좌석 정보를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("토큰이 없는 유저가 예약을 시도하면 예외가 발생한다")
    void reserve_fail_if_token_missing() {
        // given
        CreateReservationCommand command = new CreateReservationCommand(
                "invalid-token-id", userId, concertSeatId, BigDecimal.valueOf(10000));

        // when //then
        assertThatThrownBy(() -> reservationCommandService.reserve(command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("토큰 정보가 없습니다");
    }

    @Test
    @DisplayName("예약 완료 시 이벤트가 발행되고 예약 정보가 전송된다")
    void publish_event_send_reservation_info_when_reservation_completed() {
        // given
        redisTemplate.opsForZSet().add("reservation:queue", tokenId, 0L);
        tokenCommandService.activateEligibleTokens(1);

        CreateReservationCommand command = new CreateReservationCommand(
                tokenId, userId, concertSeatId, BigDecimal.valueOf(10000));

        // when
        Reservation reservation = reservationCommandService.reserve(command);

        // then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(reservationInfoSender.isCalled()).isTrue()
        );
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public TestReservationInfoSender testReservationInfoSender() {
            return new TestReservationInfoSender();
        }
    }

    public static class TestReservationInfoSender extends ReservationInfoSender {
        private boolean called = false;

        @Override
        public void send(Reservation reservation) {
            log.info("[TestReservationInfoSender] called with reservationId={}", reservation.getId());
            called = true;
        }

        public boolean isCalled() {
            return called;
        }
    }
}
