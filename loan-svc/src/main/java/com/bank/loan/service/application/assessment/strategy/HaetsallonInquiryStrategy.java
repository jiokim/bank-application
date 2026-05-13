package com.bank.loan.service.application.assessment.strategy;

import com.bank.loan.client.kifa.KifaClient;
import com.bank.loan.client.kifa.KifaGuaranteeInfo;
import com.bank.loan.service.application.assessment.dto.LoanAssessmentContext;
import com.bank.loan.core.domain.model.LnInquiryResult;
import com.bank.loan.service.application.assessment.dto.LoanAssessmentContext;
import com.bank.loan.core.domain.model.LnInquiryResultImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class HaetsallonInquiryStrategy implements LnAssessmentStrategy {

    private final KifaClient kifaClient;

    @Override
    public Long supportedPdId() { return 3L; }

    @Override
    public CompletableFuture<LnInquiryResult> assess(LoanAssessmentContext context) {
        return CompletableFuture.supplyAsync(() -> kifaClient.getGuarantee(context.custId()))
                .thenApply(kifa -> new LnInquiryResultImpl(3L, kifa.getGuaranteeLimit(), kifa.getGuaranteeRate()));
    }
}
