package kr.hhplus.be.server.presentation.payment;

import jakarta.validation.Valid;
import kr.hhplus.be.server.application.payment.PaymentCommandService;
import kr.hhplus.be.server.application.payment.PaymentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import kr.hhplus.be.server.domain.payment.Payment;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;

    /**
     * [POST] /payments
     * 결제 요청 API
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> pay(
            @RequestBody @Valid CreatePaymentRequest request
    ) {
        // TODO: 사용자 인증 후 userId 전달받는 구조로 개선
        Long userId = 1L; // 임시 고정
        Payment payment = paymentCommandService.pay(request.toCommand(userId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaymentResponse.from(payment));
    }

    /**
     * [GET] /payments?userId=1
     * 사용자 ID로 결제 내역 조회
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getPayments(@RequestParam Long userId) {
        List<PaymentResponse> payments = paymentQueryService.findByUserId(userId).stream()
                .map(PaymentResponse::from)
                .toList();
        return ResponseEntity.ok(payments);
    }

    /**
     * [PUT] /cancel
     */
    @PutMapping("/cancel/{paymentId}")
    public ResponseEntity<PaymentResponse> cancel(@RequestParam Long paymentId) {
        Payment payment = paymentCommandService.cancel(paymentId);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
}
