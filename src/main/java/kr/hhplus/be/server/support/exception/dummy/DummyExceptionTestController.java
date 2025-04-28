package kr.hhplus.be.server.support.exception.dummy;

import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dummy-exception")
public class DummyExceptionTestController {

    @GetMapping("/custom")
    public void throwCustomException() {
        throw new CustomException(ErrorCode.NOT_FOUND);  // 404 에러 유발
    }

    @GetMapping("/optimistic-lock")
    public void throwOptimisticLockException() {
        throw new TransactionSystemException("Transaction error", new OptimisticLockException());  // 409 에러 유발
    }

    @GetMapping("/general")
    public void throwGeneralException() {
        throw new RuntimeException("Unexpected error");  // 500 에러 유발
    }
}
