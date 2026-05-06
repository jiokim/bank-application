package com.bank.loan.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "한도조회 요청")
public class LoanInquiryRequest {

    @NotNull
    @Schema(description = "고객 ID", example = "1")
    private Long custId;

    @NotEmpty
    @Schema(description = "조회할 상품 ID 목록 (최대 3개)", example = "[1, 2, 3]")
    private List<Long> pdIds;
}
