package kr.hhplus.be.server.presentation.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 응답 DTO")
public record PaymentResponse(

        @Schema(description = "결제 ID", example = "5001")
        Long paymentId,

        @Schema(description = "결제 완료 시각", example = "2025-05-01T13:20:00")
        String paidAt

) {}