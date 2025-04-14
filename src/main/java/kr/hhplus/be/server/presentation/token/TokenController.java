package kr.hhplus.be.server.presentation.token;

import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.application.token.TokenQueryService;
import kr.hhplus.be.server.domain.token.QueueToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/token")

public class TokenController {

    private final TokenCommandService tokenCommandService;
    private final TokenQueryService tokenQueryService;

    public TokenController(TokenCommandService tokenCommandService,
                           TokenQueryService tokenQueryService) {
        this.tokenCommandService = tokenCommandService;
        this.tokenQueryService = tokenQueryService;
    }

    /**
     * [POST] /token/{userId}
     * - 유저 ID로 대기열 토큰을 발급한다.
     * - 기본 상태는 WAITING이며, 이후 활성화 요청이 필요
     */
    @PostMapping("/{userId}")
    public ResponseEntity<QueueTokenStatusResponse> issue(@PathVariable Long userId) {
        QueueToken token = tokenCommandService.issue(userId);
        int position = tokenQueryService.getWaitingPosition(userId).orElse(0);

        return ResponseEntity.ok(new QueueTokenStatusResponse(
                userId,
                token.getStatus(),
                position
        ));
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
     */
    @GetMapping("/{userId}/status")
    public ResponseEntity<QueueToken> status(@PathVariable Long userId) {
        Optional<QueueToken> token = tokenCommandService.status(userId);
        return token.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * [GET] /token/{userId}/position
     * - 대기열에서 현재 유저의 순서를 조회한다.
     */
    @GetMapping("/{userId}/position")
    public ResponseEntity<Integer> getWaitingPosition(@PathVariable Long userId) {
        Optional<Integer> position = tokenQueryService.getWaitingPosition(userId);
        return position.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

}
