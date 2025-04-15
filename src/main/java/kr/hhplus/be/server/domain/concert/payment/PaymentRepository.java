package kr.hhplus.be.server.domain.concert.payment;

import kr.hhplus.be.server.domain.concert.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository{
    List<Payment> findByUserId(Long userId);
    Optional<Payment> findById(Long id);
    Payment save(Payment payment);
}
