package com.bank.loan.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "휴대폰 인증번호 확인 응답")
public record LoanPhoneVerifyResponse(

        @Schema(description = "인증 성공 여부")
        boolean verified

) {}