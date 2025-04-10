package kr.hhplus.be.server.application.concert.reservation.token;

import kr.hhplus.be.server.domain.concert.reservation.token.QueueToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/token")

public class TokenController {

    private final TokenCommandService tokenCommandService;

    public TokenController(TokenCommandService tokenCommandService) {
        this.tokenCommandService = tokenCommandService;
    }

    /**
     * [POST] /token/{userId}
     * - 유저 ID로 대기열 토큰을 발급한다.
     * - 기본 상태는 WAITING이며, 이후 활성화 요청이 필요
     */
    @PostMapping("/{userId}")
    public ResponseEntity<QueueToken> issue(@PathVariable Long userId) {
        QueueToken token = tokenCommandService.issue(userId);
        return ResponseEntity.ok(token);
    }

    /**
     * [POST] /token/activate/{userId}
     * - 토큰 상태를 ACTIVE로 변경한다.
     * - 유효시간이 초과되었을 경우 예외 발생
     */
    @PostMapping("/activate/{userId}")
    public ResponseEntity<Void> activate(@PathVariable Long userId) {
        tokenCommandService.activate(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * [GET] /token/{userId}/status
     * - 유저의 현재 대기열 토큰 상태를 조회한다.
     * - 토큰이 없을 경우 204 No Content
     */
    @GetMapping("/{userId}/status")
    public ResponseEntity<QueueToken> status(@PathVariable Long userId) {
        Optional<QueueToken> token = tokenCommandService.status(userId);
        return token.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

}
