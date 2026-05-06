package com.bank.loan.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Schema(description = "대출 실행 요청")
public class LoanApplyRequest {

    @NotNull
    @Schema(description = "한도조회 ID (inquiry API 응답값)", example = "1")
    private Long inquiryId;

    @NotNull
    @Schema(description = "실행할 상품 ID", example = "1")
    private Long pdId;

    @NotNull
    @Positive
    @Schema(description = "신청 대출금액 (원)", example = "30000000")
    private BigDecimal lnAmt;
}
