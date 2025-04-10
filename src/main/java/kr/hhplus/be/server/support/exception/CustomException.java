package kr.hhplus.be.server.support.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String customMessage;

    // 기본 메시지를 사용할 경우
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = errorCode.getMessage();
    }

    // 커스텀 메시지를 추가하고 싶은 경우
    public CustomException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getCustomMessage() {
        return customMessage;
    }
}