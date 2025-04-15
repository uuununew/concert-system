package kr.hhplus.be.server.domain.cash;

import kr.hhplus.be.server.domain.concert.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CashHistoryRepository{
    List<CashHistory> findByUserId(Long userId);
    CashHistory save(CashHistory history);
}