package kr.hhplus.be.server.support.exception;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 400 Bad Request
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "E400", "잘못된 요청입니다"),
    INVALID_CONCERT_STATUS(HttpStatus.BAD_REQUEST, "E405", "콘서트 상태가 올바르지 않습니다"),
    ALREADY_PAID(HttpStatus.BAD_REQUEST, "E406", "이미 결제된 예약입니다"),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E401", "인증이 필요합니다"),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "E403", "접근 권한이 없습니다"),

    // 404 Not Found
    NOT_FOUND(HttpStatus.NOT_FOUND, "E404", "요청한 리소스를 찾을 수 없습니다"),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "E404_TOKEN", "사용자의 토큰이 존재하지 않습니다."),

    // 409 Conflict
    ALREADY_RESERVED(HttpStatus.CONFLICT, "E409", "이미 예약된 좌석입니다"),
    INVALID_TOKEN_ORDER(HttpStatus.CONFLICT,"E409_TOKEN", "아직 차례가 아닙니다."),
    CONCURRENT_REQUEST(HttpStatus.CONFLICT, "E410", "요청이 동시에 처리되어 실패했습니다. 다시 시도해 주세요"),
    OPTIMISTIC_LOCK_CONFLICT(HttpStatus.CONFLICT, "E411", "충돌이 발생했습니다. 다시 시도해주세요"),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E500", "내부 서버 오류"),
    CACHE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "E502", "공연 목록 데이터를 저장할 캐시 공간을 찾을 수 없습니다."),
    INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, "E501", "요청 처리 중 잠시 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
