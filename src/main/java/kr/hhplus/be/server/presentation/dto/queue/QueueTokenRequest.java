package kr.hhplus.be.server.presentation.dto.queue;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대기열 토큰 요청 DTO")
public class QueueTokenRequest {
    @Schema(description = "유저 ID", example = "42", required = true)
    private Long userId;

    public QueueTokenRequest() {}

    public QueueTokenRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
