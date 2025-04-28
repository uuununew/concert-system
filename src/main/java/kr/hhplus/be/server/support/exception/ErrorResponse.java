package kr.hhplus.be.server.support.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse {

    private final String code;
    private final String message;

    //명시적 생성자
    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    //ErrorCode 기반 생성자 추가
    public ErrorResponse(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}