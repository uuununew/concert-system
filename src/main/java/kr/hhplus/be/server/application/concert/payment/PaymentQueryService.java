package kr.hhplus.be.server.application.concert.payment;

import kr.hhplus.be.server.domain.concert.payment.Payment;
import kr.hhplus.be.server.domain.concert.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;

    /**
     * 유저별 결제 내역을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 결제 목록
     */
    public List<Payment> findByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

}
