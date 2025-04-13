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

    // 테스트 전용 팩토리 메서드
    public static Concert withStatus(ConcertStatus status) {
        Concert concert = new Concert();
        concert.status = status;
        concert.updatedAt = LocalDateTime.now();
        return concert;
    }

    /**
     * 콘서트를 예약 가능 상태로 연다
     */
    public void open() {
        if (this.status != ConcertStatus.READY) {
            throw new CustomException(ErrorCode.INVALID_CONCERT_STATUS, "콘서트는 READY 상태여야 열 수 있습니다.");
        }
        this.status = ConcertStatus.OPENED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 콘서트를 종료한다
     */
    public void close() {
        if (this.status != ConcertStatus.OPENED) {
            throw new CustomException(ErrorCode.INVALID_CONCERT_STATUS, "콘서트는 OPENED 상태여야 종료할 수 있습니다.");
        }
        this.status = ConcertStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 콘서트를 취소한다
     */
    public void cancel() {
        if (this.status == ConcertStatus.CANCELED || this.status == ConcertStatus.CLOSED) {
            throw new CustomException(ErrorCode.INVALID_CONCERT_STATUS, "READY 또는 OPENED 상태만 취소할 수 있습니다.");
        }
        this.status = ConcertStatus.CANCELED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOpened() {
        return this.status == ConcertStatus.OPENED;
    }
}
