package com.bank.loan.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "휴대폰 인증번호 확인 요청")
public class LoanPhoneVerifyRequest {

    @NotNull
    @Schema(description = "고객 ID", example = "1")
    private Long custId;

    @NotBlank
    @Schema(description = "휴대폰 번호", example = "01012345678")
    private String phoneNo;

    @NotBlank
    @Schema(description = "인증 토큰 (인증번호 발송 시 발급)", example = "a1b2c3d4-...")
    private String verifyToken;

    @NotBlank
    @Schema(description = "인증번호 (6자리)", example = "123456")
    private String verifyCode;

    @NotEmpty
    @Schema(description = "동의한 약관 ID 목록", example = "[1, 2, 3]")
    private List<Long> agreedTermIds;
}