package com.bank.loan.controller.dto;

import com.bank.common.sensitive.Sensitive;
import com.bank.common.sensitive.StoragePolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "휴대폰 인증번호 발송 요청")
public class LoanPhoneSendRequest {

    @NotNull
    @Schema(description = "고객 ID", example = "1")
    private Long custId;

    @NotBlank
    @Sensitive(storagePolicy = StoragePolicy.ENCRYPT)
    @Schema(description = "휴대폰 번호", example = "01012345678")
    private String phoneNo;

}