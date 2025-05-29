package kr.hhplus.be.server.presentation.token;

import jakarta.servlet.http.HttpServletRequest;
import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.application.token.TokenQueryService;
import kr.hhplus.be.server.application.token.WaitingTokenPublisher;
import kr.hhplus.be.server.domain.token.TokenStatus;
import kr.hhplus.be.server.infrastructure.token.WaitingTokenKafkaProducer;
import kr.hhplus.be.server.infrastructure.token.WaitingTokenRequestMessage;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenQueryService tokenQueryService;
    private final WaitingTokenPublisher waitingTokenPublisher;

    /**
     * [POST] /token/
     * - 대기열 토큰을 새로 발급한다 (UUID 기반)
     * - 기본 상태는 WAITING
     */
    @PostMapping
    public ResponseEntity<String> issue(HttpServletRequest request, @RequestParam("concertId") Long concertId) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        //Kafka 메시지 발행
        WaitingTokenRequestMessage message = new WaitingTokenRequestMessage(
                userId,
                concertId,
                LocalDateTime.now()
        );
        waitingTokenPublisher.publish(message); // Kafka Producer 사용

        return ResponseEntity.accepted().build();
    }

    /**
     * [GET] /token/{tokenId}/status
     * - 토큰이 대기열에 있으면 "WAITING", 없으면 "ACTIVE"로 응답
     */
    @GetMapping("/{tokenId}/status")
    public ResponseEntity<TokenStatus> status(@PathVariable String tokenId) {
        Optional<Integer> position = tokenQueryService.getWaitingPosition(tokenId);
        TokenStatus status = position.isPresent() ? TokenStatus.WAITING : TokenStatus.ACTIVE;
        return ResponseEntity.ok(status);
    }
}
