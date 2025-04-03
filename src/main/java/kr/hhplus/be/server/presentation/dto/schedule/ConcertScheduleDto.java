package kr.hhplus.be.server.presentation.dto.schedule;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "콘서트 회차 일정 DTO")
public record ConcertScheduleDto (

        @Schema(description = "회차 ID", example = "101")
        Long scheduleId,

        @Schema(description = "콘서트 ID", example = "1")
        Long concertId,

        @Schema(description = "공연 날짜", example = "2025-05-01")
        String date,

        @Schema(description = "공연 시작 시간", example = "19:00")
        String startTime,

        @Schema(description = "예매 가능 여부", example = "true")
        boolean available

) {}
