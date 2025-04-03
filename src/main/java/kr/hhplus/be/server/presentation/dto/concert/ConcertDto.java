package kr.hhplus.be.server.presentation.dto.concert;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "콘서트 기본 정보 DTO")
public record ConcertDto(
        @Schema(description = "콘서트 ID", example = "1")
        Long id,

        @Schema(description = "콘서트 이름", example = "BTS WORLD TOUR")
        String name,

        @Schema(description = "공연 시간", example = "2025-06-01T19:00:00")
        LocalDateTime startAt
) {}