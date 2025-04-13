package kr.hhplus.be.server.domain.concert;

import java.util.List;
import java.util.Optional;

public interface ConcertRepository {
    //콘서트 저장
    Concert save(Concert concert);

    //콘서트 단건 조회
    Optional<Concert> findById(Long id);

    //콘서트 전체 목록 리스트
    List<Concert> findAll();

    //주어진 ID의 콘서트가 존재하는지 확인
    boolean existsById(Long concertId);
}
