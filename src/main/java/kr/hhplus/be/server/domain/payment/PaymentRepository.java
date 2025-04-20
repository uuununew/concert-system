package kr.hhplus.be.server.domain.payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository{
    List<Payment> findByUserId(Long userId);
    Optional<Payment> findById(Long id);
    Payment save(Payment payment);
}
