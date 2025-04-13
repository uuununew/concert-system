package kr.hhplus.be.server.domain.concert.payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    /**
     * 결제 정보 저장
     */
    Payment save(Payment payment);

    /**
     * 결제 조회
     */
    List<Payment> findByUserId(Long userId);

    /**
     * 결제 ID로 단일 결제 정보를 조회
     */
    Optional<Payment> findById(Long id);
}
