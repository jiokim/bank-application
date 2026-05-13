package com.bank.loan.service.application.assessment.strategy;

import com.bank.loan.service.application.assessment.dto.LoanAssessmentContext;
import com.bank.loan.core.domain.model.LnInquiryResult;
import com.bank.loan.service.application.assessment.dto.LoanAssessmentContext;
import com.bank.loan.core.domain.model.LnInquiryResultImpl;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Component
public class CreditLoanInquiryStrategy implements LnAssessmentStrategy {

    @Override
    public Long supportedPdId() { return 1L; }

    @Override
    public CompletableFuture<LnInquiryResult> assess(LoanAssessmentContext context) {
        return CompletableFuture.completedFuture(applyBankRule(context));
    }

    private LnInquiryResult applyBankRule(LoanAssessmentContext context) {
        int avgScore = (context.nice().getCreditScore() + context.kcb().getCreditScore()) / 2;
        if (avgScore >= 700) {
            return new LnInquiryResultImpl(1L, new BigDecimal("50000000"), new BigDecimal("0.045"));
        } else {
            return new LnInquiryResultImpl(1L, new BigDecimal("20000000"), new BigDecimal("0.065"));
        }
    }
}
