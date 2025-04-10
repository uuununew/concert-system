package kr.hhplus.be.server.presentation.concert;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.application.concert.CreateConcertSeatCommand;
import kr.hhplus.be.server.application.concert.UpdateConcertSeatCommand;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CreateConcertSeatRequest {

    @NotBlank(message = "좌석 번호는 필수입니다.")
    private String seatNumber;

    @NotBlank(message = "섹션은 필수입니다.")
    private String section;

    @NotBlank(message = "행 정보는 필수입니다.")
    private String row;

    @NotBlank(message = "좌석 등급은 필수입니다.")
    private String grade;

    @NotNull(message = "가격은 필수입니다.")
    @DecimalMin(value = "1", inclusive = true, message = "가격은 0보다 커야 합니다.")
    private BigDecimal price;


    public CreateConcertSeatCommand toCommand(Long concertId) {
        return new CreateConcertSeatCommand(
                concertId,
                seatNumber,
                section,
                row,
                grade,
                price
        );
    }
}