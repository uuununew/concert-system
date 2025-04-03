package kr.hhplus.be.server.e2e;

import kr.hhplus.be.server.presentation.dto.cash.CashBalanceResponse;
import kr.hhplus.be.server.presentation.dto.cash.CashChargeRequest;
import kr.hhplus.be.server.presentation.dto.concert.ConcertDto;
import kr.hhplus.be.server.presentation.dto.payment.PaymentRequest;
import kr.hhplus.be.server.presentation.dto.payment.PaymentResponse;
import kr.hhplus.be.server.presentation.dto.queue.QueueTokenRequest;
import kr.hhplus.be.server.presentation.dto.queue.QueueTokenResponse;
import kr.hhplus.be.server.presentation.dto.reservation.SeatReservationRequest;
import kr.hhplus.be.server.presentation.dto.reservation.SeatReservationResponse;
import kr.hhplus.be.server.presentation.dto.schedule.ConcertScheduleDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class E2EApiTest {
    @Autowired
    TestRestTemplate restTemplate;


    @Test
    void 대기열_토큰_발급_성공(){
        var request = new QueueTokenRequest(42L);
        ResponseEntity<QueueTokenResponse> response = restTemplate.postForEntity("/queue/token", request, QueueTokenResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void 콘서트_목록_조회_성공() {
        ResponseEntity<ConcertDto[]> response = restTemplate.getForEntity("/concerts", ConcertDto[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void 콘서트_회차_일정_조회_성공(){
        ResponseEntity<ConcertScheduleDto[]>response = restTemplate.getForEntity("/schedules?concertId=1", ConcertScheduleDto[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void 좌석_예약_성공(){
        var request = new SeatReservationRequest(1L, 101L, "A-10", 42L);
        ResponseEntity<SeatReservationResponse> response = restTemplate.postForEntity("/reservations", request, SeatReservationResponse.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void 잔액_조회_성공(){
        ResponseEntity<CashBalanceResponse>response = restTemplate.getForEntity("/cash?userId=42", CashBalanceResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void 충전_성공(){
        var request = new CashChargeRequest(42L, 10000);
        ResponseEntity<CashBalanceResponse> response = restTemplate.postForEntity("/cash/charge", request, CashBalanceResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void 결제_성공(){
        var request = new PaymentRequest(42L, 9001L, 10000);
        ResponseEntity<PaymentResponse>response = restTemplate.postForEntity("/payments", request, PaymentResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
