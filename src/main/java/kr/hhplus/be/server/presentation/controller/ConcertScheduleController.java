package kr.hhplus.be.server.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.common.ErrorResponse;
import kr.hhplus.be.server.presentation.dto.schedule.ConcertScheduleDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/schedules")
@Tag(name = "Schedule", description = "콘서트 회차 일정 조회 API")
public class ConcertScheduleController {

    @Operation(
            summary = "회차 일정 목록 조회",
            description = "concertId에 해당하는 콘서트의 회차 일정을 반환합니다.",
            parameters = {
                    @Parameter(name = "concertId", description = "콘서트 ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ConcertScheduleDto.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "콘서트를 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<List<ConcertScheduleDto>> getSchedules(@RequestParam Long concertId) {
        List<ConcertScheduleDto> schedules = List.of(
                new ConcertScheduleDto(101L, concertId, "2025-05-01", "19:00", true),
                new ConcertScheduleDto(102L, concertId, "2025-05-02", "19:00", false)
        );
        return ResponseEntity.ok(schedules);
    }
}
