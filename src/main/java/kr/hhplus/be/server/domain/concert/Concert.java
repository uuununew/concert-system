package kr.hhplus.be.server.domain.concert;

import jakarta.persistence.*;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.*;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer round;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConcertStatus status;

    @Column(nullable = false)
    private LocalDateTime concertDateTime;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Concert(String title, Integer round, ConcertStatus status, LocalDateTime concertDateTime) {
        this.title = title;
        this.round = round;
        this.status = status;
        this.concertDateTime = concertDateTime;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Concert create(String title, Integer round, ConcertStatus status, LocalDateTime concertDateTime) {
        if (title == null || title.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "제목은 필수입니다.");
        }
        if (round == null || round <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "회차는 1 이상이어야 합니다.");
        }
        if (status == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "상태는 필수입니다.");
        }
        if (concertDateTime == null || concertDateTime.isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "콘서트 시작 시간은 현재 이후여야 합니다.");
        }
        return new Concert(title, round, status, concertDateTime);
    }

    /**
     * 콘서트가 예약 가능한 상태인지 확인
     * 현재 상태가 OPEND일 때만 가능
     */
    public boolean isOpened() {
        return this.status == ConcertStatus.OPENED;
    }

    /**
     * 콘서트 상태 변경 메서드
     * - 상태 변경 시 updatedAt을 갱신
     */
    public void changeStatus(ConcertStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }
}
