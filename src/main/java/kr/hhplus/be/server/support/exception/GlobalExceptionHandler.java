package kr.hhplus.be.server.support.exception;

import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.validation.ObjectError;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 에러 (CustomException)
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("CustomException 발생! errorCode = {}", e.getErrorCode());
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode));
    }

    /**
     * 트랜잭션 에러 (OptimisticLock 포함)
     */
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ErrorResponse> handleTransactionSystemException(TransactionSystemException e) {
        Throwable rootCause = e.getRootCause();
        log.error("TransactionSystemException occurred", e);

        if (rootCause instanceof OptimisticLockException || rootCause instanceof OptimisticLockingFailureException) {
            return ResponseEntity
                    .status(ErrorCode.OPTIMISTIC_LOCK_CONFLICT.getStatus())
                    .body(new ErrorResponse(ErrorCode.OPTIMISTIC_LOCK_CONFLICT));
        }

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    /**
     * 그 외 예상하지 못한 에러
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("Unhandled Exception occurred", e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}