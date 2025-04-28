package kr.hhplus.be.server.support.interceptor;

import kr.hhplus.be.server.domain.concert.*;
import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;



import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TokenValidationInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Concert concert = new Concert(
                "콜드플레이", 1, ConcertStatus.OPENED, LocalDateTime.now());
        concertRepository.save(concert);

        ConcertSeat seat = ConcertSeat.withAll(
                1L, concert, "A", "10", "1", "VIP", BigDecimal.valueOf(10000), SeatStatus.AVAILABLE, LocalDateTime.now());
        concertSeatRepository.save(seat);
    }

    @Test
    @DisplayName("토큰이 정상일 경우 요청이 성공한다")
    void request_succeeds_with_valid_token() throws Exception {
        mockMvc.perform(post("/token/123")
                        .header("X-USER-ID", "123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        QueueToken token = tokenRepository.findByUserId(123L).orElseThrow();
        token.activate();
        tokenRepository.save(token);

        ReservationRequest request = new ReservationRequest(123L, 1L, 1L, 10000);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/reservations")
                        .header("X-USER-ID", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("X-USER-ID 헤더가 없으면 예외가 발생한다")
    void throws_exception_when_userId_header_missing() throws Exception {
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("E400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"));
    }

    @Test
    @DisplayName("잘못된 형식의 X-USER-ID가 오면 예외가 발생한다")
    void throws_exception_when_userId_is_invalid_format() throws Exception {
        mockMvc.perform(post("/reservations")
                        .header("X-USER-ID", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("E400"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다"));
    }

    @Test
    @DisplayName("토큰이 존재하지 않으면 예외가 발생한다")
    void throws_exception_when_token_not_found() throws Exception {
        ReservationRequest request = new ReservationRequest(9999L, 1L, 1L, 10000);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/reservations")
                        .header("X-USER-ID", "9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("E404-TOKEN"))
                .andExpect(jsonPath("$.message").value("사용자의 토큰이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("토큰 상태가 ACTIVE가 아니면 예외가 발생한다")
    void throws_exception_when_token_status_not_active() throws Exception {
        mockMvc.perform(post("/token/456")
                        .header("X-USER-ID", "456")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ReservationRequest request = new ReservationRequest(456L, 1L, 1L, 10000);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/reservations")
                        .header("X-USER-ID", "456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("E404-TOKEN"))
                .andExpect(jsonPath("$.message").value("사용자의 토큰이 존재하지 않습니다."));
    }

    // 내부 DTO 정의
    public record ReservationRequest(Long userId, Long concertSeatId, Long concertId, int price) {}
}

