package com.bank.loan.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "휴대폰 인증번호 발송 응답")
public record LoanPhoneSendResponse(

        @Schema(description = "발송 성공 여부")
        boolean sent,

        @Schema(description = "인증 토큰 (인증번호 확인 시 사용)", example = "a1b2c3d4-...")
        String verifyToken,

        @Schema(description = "휴대폰 번호", example = "01012345678")
        String phoneNo

) {}