package kr.hhplus.be.server.domain.concert;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConcertRepository{
    Optional<Concert> findById(Long id);
    Concert save(Concert concert);
    List<Concert> findAll();
    boolean existsById(Long id);
    Page<Concert> findAll(Pageable pageable);
    void deleteById(Long id);
    void deleteAll();
}
