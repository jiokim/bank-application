package com.bank.loan.controller.dto;

import com.bank.common.sensitive.Sensitive;
import com.bank.common.sensitive.StoragePolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "대출신청 실명인증 요청")
public class LoanRealNameVerificationRequest {

    @Schema(description = "고객명", example = "홍길동")
    private String custNm;

    @Sensitive(storagePolicy = StoragePolicy.ENCRYPT)
    @Schema(description = "실명번호 (주민등록번호 13자리)", example = "8901011000001")
    private String rnmNbr;
}
