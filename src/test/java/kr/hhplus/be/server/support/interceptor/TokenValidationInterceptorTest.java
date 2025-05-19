package kr.hhplus.be.server.support.interceptor;

import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.domain.concert.*;
import kr.hhplus.be.server.domain.token.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
    private TokenCommandService tokenCommandService;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private Long seatId;
    private Long concertId;

    @BeforeEach
    void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @BeforeEach
    void setUp() {
        Concert concert = concertRepository.save(new Concert(
                "콜드플레이", 1, ConcertStatus.OPENED, LocalDateTime.now()));
        concertId = concert.getId();

        ConcertSeat seat = concertSeatRepository.save(ConcertSeat.of(
                concert, "A", "10", "1", "VIP", BigDecimal.valueOf(10000)));

        seatId = seat.getId();
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
    @DisplayName("토큰이 아직 대기 중이면 예외 발생")
    void throws_exception_when_token_still_waiting() throws Exception {
        String tokenId = tokenCommandService.issue(456L); // 활성화 안 됨

        ReservationRequest request = new ReservationRequest(456L, seatId, concertId, 10000);
        String json = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/reservations")
                        .header("X-USER-ID", "456")
                        .header("X-TOKEN-ID", tokenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("E400"))
                .andExpect(jsonPath("$.message").value("아직 대기 중인 토큰입니다."));
    }

    // 내부 DTO 정의
    public record ReservationRequest(Long userId, Long concertSeatId, Long concertId, int price) {}
}

