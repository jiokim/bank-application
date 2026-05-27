package com.bank.loan.controller;

import com.bank.loan.controller.dto.LoanRealNameVerificationRequest;
import com.bank.loan.controller.dto.LoanRealNameVerificationResponse;
import com.bank.loan.service.application.verification.LoanCustVerifyService;
import com.bank.loan.service.application.verification.dto.LoanRealNameVerifyCommand;
import com.bank.loan.service.application.verification.dto.LoanRealNameVerifyInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "대출신청 실명인증", description = "실명인증 및 대출신청 가능여부 확인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/loans")
public class LoanCustVerifyController {

    private final LoanCustVerifyService loanCustVerifyService;

    @Operation(summary = "실명인증",
            description = "고객명과 실명번호로 실명인증을 수행합니다.")
    @PostMapping("/real-name-verification")
    public LoanRealNameVerificationResponse verify(@Valid @RequestBody LoanRealNameVerificationRequest request) {
        LoanRealNameVerifyInfo info = loanCustVerifyService.verifyRealName(
                new LoanRealNameVerifyCommand(request.getCustNm(), request.getRnmNbr()));
        return new LoanRealNameVerificationResponse(info.verified(), info.custId());
    }
}
