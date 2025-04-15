package kr.hhplus.be.server.infrastructure.concert.payment;

import kr.hhplus.be.server.domain.concert.payment.Payment;
import kr.hhplus.be.server.domain.concert.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public List<Payment> findByUserId(Long userId) {
        return paymentJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return paymentJpaRepository.findById(id);
    }

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }
}
