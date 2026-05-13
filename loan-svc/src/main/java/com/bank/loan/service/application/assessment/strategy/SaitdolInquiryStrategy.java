package com.bank.loan.service.application.assessment.strategy;

import com.bank.loan.client.sgi.SgiClient;
import com.bank.loan.service.application.assessment.dto.LoanAssessmentContext;
import com.bank.loan.core.domain.model.LnInquiryResult;
import com.bank.loan.service.application.assessment.dto.LoanAssessmentContext;
import com.bank.loan.core.domain.model.LnInquiryResultImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class SaitdolInquiryStrategy implements LnAssessmentStrategy {

    private final SgiClient sgiClient;

    @Override
    public Long supportedPdId() {
        return 2L;
    }

    @Override
    public CompletableFuture<LnInquiryResult> assess(LoanAssessmentContext context) {
        return CompletableFuture.supplyAsync(() -> sgiClient.getGuarantee(context.custId()))
                .thenApply(sgi -> new LnInquiryResultImpl(2L, sgi.getGuaranteeLimit(), sgi.getGuaranteeRate()));
    }
}
