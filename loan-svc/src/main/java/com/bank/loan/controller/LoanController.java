package com.bank.loan.controller;

import com.bank.loan.service.LoanCommandService;
import com.bank.loan.service.dto.LoanApplyRequest;
import com.bank.loan.service.dto.LoanApplyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/loans")
public class LoanController {

    private final LoanCommandService loanCommandService;

    @PostMapping
    public LoanApplyResponse apply(@Valid @RequestBody LoanApplyRequest request) {
        return loanCommandService.apply(request);
    }
}
