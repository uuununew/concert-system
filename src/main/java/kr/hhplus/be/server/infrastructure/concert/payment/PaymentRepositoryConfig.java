package kr.hhplus.be.server.infrastructure.concert.payment;

import kr.hhplus.be.server.domain.concert.payment.Payment;
import kr.hhplus.be.server.domain.concert.payment.PaymentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

@Configuration
public class PaymentRepositoryConfig {

        @Bean
        public PaymentRepository paymentRepository(PaymentJpaRepository jpaRepository) {
            return new PaymentRepository() {
                @Override
                public Payment save(Payment payment) {
                    return jpaRepository.save(payment);
                }

                @Override
                public Optional<Payment> findById(Long id) {
                    return jpaRepository.findById(id);
                }

                @Override
                public List<Payment> findByUserId(Long userId) {
                    return jpaRepository.findByUserId(userId);
                }
            };
        }
}
