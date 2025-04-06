package kr.hhplus.be.server.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "에러 응답")
public class ErrorResponse {
    @Schema(description = "에러 메시지", example = "잘못된 요청입니다")
    private String message;

    @Schema(description = "HTTP 상태 코드", example = "400")
    private int status;

    @Schema(description = "에러 코드", example = "INVALID_REQUEST")
    private String errorCode;

    @Schema(description = "에러 발생 시간", example = "2025-04-03T13:00:00Z")
    private String timestamp;

    public ErrorResponse(String message, int status, String errorCode) {
        this.message = message;
        this.status = status;
        this.errorCode = errorCode;
        this.timestamp = java.time.ZonedDateTime.now().toString();
    }
}