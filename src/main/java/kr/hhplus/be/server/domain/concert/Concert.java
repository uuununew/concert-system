package kr.hhplus.be.server.domain.concert;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
public class Concert {

    private Long id;
    private String title;
    private Integer round;
    private ConcertStatus status;
    private LocalDateTime concertDateTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 콘서트가 예약 가능한 상태인지 확인
     * 현재 상태가 OPEND일 때만 가능
     */
    public boolean isOpened() {
        return this.status == ConcertStatus.OPENED;
    }
}
