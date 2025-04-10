package kr.hhplus.be.server.presentation.concert;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.application.concert.UpdateConcertSeatCommand;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 좌석 정보 수정 요청 DTO
 */
@Getter
public class UpdateConcertSeatRequest {

    @NotBlank(message = "좌석 번호는 필수입니다.")
    private String seatNumber;

    @NotBlank(message = "구역은 필수입니다.")
    private String section;

    @NotBlank(message = "행 정보는 필수입니다.")
    private String row;

    @NotBlank(message = "등급은 필수입니다.")
    private String grade;

    @NotNull(message = "가격은 필수입니다.")
    @DecimalMin(value = "1", message = "가격은 0보다 커야 합니다.")
    private BigDecimal price;

    // 기본 생성자 (JSON 역직렬화를 위한)
    public UpdateConcertSeatRequest() {
    }

    public UpdateConcertSeatRequest(String seatNumber, String section, String row, String grade, BigDecimal price) {
        this.seatNumber = seatNumber;
        this.section = section;
        this.row = row;
        this.grade = grade;
        this.price = price;
    }

    public UpdateConcertSeatCommand toCommand() {
        return new UpdateConcertSeatCommand(seatNumber, section, row, grade, price);
    }
}
