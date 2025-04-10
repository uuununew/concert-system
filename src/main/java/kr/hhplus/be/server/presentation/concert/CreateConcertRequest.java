package kr.hhplus.be.server.presentation.concert;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.hhplus.be.server.application.concert.CreateConcertCommand;
import kr.hhplus.be.server.domain.concert.ConcertStatus;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 콘서트 생성 요청 DTO
 */
@Getter
public class CreateConcertRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotNull(message = "회차는 필수입니다.")
    @Positive(message = "회차는 1 이상이어야 합니다.")
    private Integer round;

    @NotNull(message = "상태는 필수입니다.")
    private ConcertStatus status;

    @NotNull(message = "공연일시는 필수입니다.")
    @Future(message = "공연 일시는 현재보다 이후여야 합니다.")
    private LocalDateTime concertDateTime;

    // 기본 생성자
    public CreateConcertRequest() {}

    public CreateConcertRequest(String title, Integer round, ConcertStatus status, LocalDateTime concertDateTime) {
        this.title = title;
        this.round = round;
        this.status = status;
        this.concertDateTime = concertDateTime;
    }
     //command를 객체로 변환
    public CreateConcertCommand toCommand() {
        return new CreateConcertCommand(title, round, status, concertDateTime);
    }
}
