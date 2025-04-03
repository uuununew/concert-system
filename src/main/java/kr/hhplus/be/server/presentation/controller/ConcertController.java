package kr.hhplus.be.server.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.common.ErrorResponse;
import kr.hhplus.be.server.presentation.dto.concert.ConcertDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/concerts")
@Tag(name = "Concert", description = "콘서트 API")
public class ConcertController {

    @Operation(
            summary = "콘서트 목록 조회",
            description = "등록된 모든 콘서트 회차 정보를 목록 형태로 반환합니다.\n" +
                    "각 회차는 ID, 콘서트 이름, 시작 시간을 포함합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ConcertDto.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @GetMapping
    public ResponseEntity<List<ConcertDto>> getConcerts() {
        List<ConcertDto> concerts = List.of(
                new ConcertDto(1L, "BLACKPINK", LocalDateTime.of(2025, 5, 1, 19, 0)),
                new ConcertDto(2L, "BTS", LocalDateTime.of(2025, 6, 10, 18, 30)),
                new ConcertDto(3L, "IU", LocalDateTime.of(2025, 7, 10, 20, 0))
        );
        return ResponseEntity.ok(concerts);
    }
}
