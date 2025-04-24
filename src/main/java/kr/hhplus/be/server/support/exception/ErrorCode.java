package kr.hhplus.be.server.support.exception;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 400
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "E400", "잘못된 요청입니다"),

    //405
    INVALID_CONCERT_STATUS(HttpStatus.BAD_REQUEST, "E405", "콘서트 상태가 올바르지 않습니다"),


    // 401
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E401", "인증이 필요합니다"),

    // 403
    FORBIDDEN(HttpStatus.FORBIDDEN, "E403", "접근 권한이 없습니다"),

    // 404
    NOT_FOUND(HttpStatus.NOT_FOUND, "E404", "요청한 리소스를 찾을 수 없습니다"),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "E404-TOKEN", "사용자의 토큰이 존재하지 않습니다."),

    // 409
    ALREADY_RESERVED(HttpStatus.CONFLICT, "E409", "이미 예약된 좌석입니다"),
    INVALID_TOKEN_ORDER(HttpStatus.CONFLICT,"E409-TOKEN", "아직 차례가 아닙니다."),

    CONCURRENT_REQUEST(HttpStatus.CONFLICT, "E410", "요청이 동시에 처리되어 실패했습니다. 다시 시도해 주세요"),

    OPTIMISTIC_LOCK_CONFLICT(HttpStatus.CONFLICT, "E411", "낙관적 락 충돌이 발생했습니다. 다시 시도해주세요"),

    // 406
    ALREADY_PAID(HttpStatus.BAD_REQUEST, "E406", "이미 결제된 예약입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
