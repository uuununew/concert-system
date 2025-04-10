package kr.hhplus.be.server.infrastructure.concert.payment;

import kr.hhplus.be.server.domain.concert.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    /**
     * 사용자 ID로 결제 내역 조회
     */
    List<Payment> findByUserId(Long userId);
}
