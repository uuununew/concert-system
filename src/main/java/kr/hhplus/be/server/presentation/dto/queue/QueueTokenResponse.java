package kr.hhplus.be.server.presentation.dto.queue;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대기열 토큰 응답 DTO")
public record QueueTokenResponse(

        @Schema(description = "발급된 대기열 토큰", example = "8f2a1a10-b914-4d4f-845a-2aee07a46c44")
        String queueToken,

        @Schema(description = "토큰 발급 시각", example = "2025-05-01T12:50:00")
        String issuedAt

) {}
