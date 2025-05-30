package kr.hhplus.be.server.infrastructure.concert;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepository {

    private final ConcertJpaRepository concertJpaRepository;

    @Override
    public Optional<Concert> findById(Long id){
        return concertJpaRepository.findById(id);
    }

    @Override
    public Concert save(Concert concert){
        return concertJpaRepository.save(concert);
    }

    public List<Concert> findAll() {
        return concertJpaRepository.findAll();
    }

    @Override
    public boolean existsById(Long id) {
        return concertJpaRepository.existsById(id);
    }

    @Override
    public Page<Concert> findAll(Pageable pageable) {return concertJpaRepository.findAll(pageable);}

    @Override
    public void deleteById(Long id) {concertJpaRepository.deleteById(id);}

    @Override
    public void deleteAll() {concertJpaRepository.deleteAll();}
}
