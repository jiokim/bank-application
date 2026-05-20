package com.bank.loan.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대출신청 실명인증 응답")
public record LoanRealNameVerificationResponse(

        @Schema(description = "실명인증 성공 여부")
        boolean realNameVerified,

        @Schema(description = "고객식별자 (실명인증 성공 시)", example = "1")
        Long custId

) {}
