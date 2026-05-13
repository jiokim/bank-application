package com.bank.loan.service.application.assessment.strategy;

import com.bank.loan.service.application.assessment.dto.LoanAssessmentContext;
import com.bank.loan.core.domain.model.LnInquiryResult;

import java.util.concurrent.CompletableFuture;

public interface LnAssessmentStrategy {

    Long supportedPdId();

    CompletableFuture<LnInquiryResult> assess(LoanAssessmentContext context);
}
