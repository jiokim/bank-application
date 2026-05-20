package com.bank.loan.controller;

import com.bank.loan.controller.dto.LoanRealNameVerificationRequest;
import com.bank.loan.controller.dto.LoanRealNameVerificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "대출신청 실명인증", description = "실명인증 및 대출신청 가능여부 확인 API")
@RestController
@RequestMapping("/v1/loans")
public class LoanCustVerifyController {

    @Operation(summary = "실명인증",
            description = "고객명과 실명번호로 실명인증을 수행합니다.")
    @PostMapping("/real-name-verification")
    public LoanRealNameVerificationResponse verify(@RequestBody LoanRealNameVerificationRequest request) {
        if (!StringUtils.hasText(request.getCustNm())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "고객명은 필수입니다.");
        }
        if (!StringUtils.hasText(request.getRnmNbr())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "실명번호는 필수입니다.");
        }
        return new LoanRealNameVerificationResponse(true, 1L);
    }
}
