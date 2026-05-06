package com.bank.loan.controller.dto;

import com.bank.common.sensitive.Sensitive;
import com.bank.common.sensitive.StoragePolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank
    @Sensitive(storagePolicy = StoragePolicy.ENCRYPT)
    @Schema(description = "고객 주민등록번호 (13자리, 하이픈 없이)", example = "9001011000000")
    private String custRrn;
}
