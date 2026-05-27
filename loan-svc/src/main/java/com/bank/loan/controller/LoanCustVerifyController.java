package com.bank.loan.controller;

import com.bank.loan.controller.dto.*;
import com.bank.loan.service.application.verification.LoanCustVerifyService;
import com.bank.loan.service.application.verification.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "대출신청 본인인증", description = "실명인증 및 휴대폰인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/loans")
public class LoanCustVerifyController {

    private final LoanCustVerifyService loanCustVerifyService;

    @Operation(summary = "실명인증",
            description = "고객명과 실명번호로 실명인증을 수행합니다.")
    @PostMapping("/real-name-verification")
    public LoanRealNameVerificationResponse verifyRealName(@Valid @RequestBody LoanRealNameVerificationRequest request) {
        LoanRealNameVerifyInfo info = loanCustVerifyService.verifyRealName(
                new LoanRealNameVerifyCommand(request.getCustNm(), request.getRnmNbr()));
        return new LoanRealNameVerificationResponse(info.verified(), info.custId());
    }

    @Operation(summary = "휴대폰 인증번호 발송",
            description = "고객의 휴대폰으로 인증번호를 발송합니다.")
    @PostMapping("/phone-verification")
    public LoanPhoneSendResponse sendPhone(@Valid @RequestBody LoanPhoneSendRequest request) {
        LoanPhoneSendInfo info = loanCustVerifyService.sendPhone(
                new LoanPhoneSendCommand(request.getCustId(), request.getPhoneNo()));
        return new LoanPhoneSendResponse(info.sent(), info.verifyToken(), info.phoneNo());
    }

    @Operation(summary = "휴대폰 인증번호 확인",
            description = "발송된 인증번호를 확인합니다.")
    @PostMapping("/phone-verification/confirm")
    public LoanPhoneVerifyResponse confirmPhone(@Valid @RequestBody LoanPhoneVerifyRequest request) {
        LoanPhoneVerifyInfo info = loanCustVerifyService.verifyPhone(
                new LoanPhoneVerifyCommand(request.getCustId(), request.getPhoneNo(), request.getVerifyToken(), request.getVerifyCode(), request.getAgreedTermIds()));
        return new LoanPhoneVerifyResponse(info.verified());
    }
}
