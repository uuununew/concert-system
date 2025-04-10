package kr.hhplus.be.server.infrastructure.concert.reservation;

import kr.hhplus.be.server.domain.concert.reservation.Reservation;
import kr.hhplus.be.server.domain.concert.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.concert.reservation.ReservationStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Configuration
public class ReservationRepositoryConfig {

    @Bean
    public ReservationRepository reservationRepository(ReservationJpaRepository jpaRepository) {
        return new ReservationRepository() {

            @Override
            public Reservation save(Reservation reservation) {
                return jpaRepository.save(reservation);
            }

            @Override
            public Optional<Reservation> findByConcertSeatIdAndStatus(Long concertSeatId, ReservationStatus status) {
                return jpaRepository.findByConcertSeatIdAndStatus(concertSeatId, status);
            }

            @Override
            public Optional<Reservation> findById(Long id) {
                return jpaRepository.findById(id);
            }

            @Override
            public List<Reservation> findAllByStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime dateTime) {
                return jpaRepository.findAllByStatusAndCreatedAtBefore(status, dateTime);
            }
        };
    }
}
