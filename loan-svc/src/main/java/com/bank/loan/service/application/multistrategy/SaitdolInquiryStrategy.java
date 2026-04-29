package com.bank.loan.service.application.multistrategy;

import com.bank.loan.client.kcb.KcbCreditInfo;
import com.bank.loan.client.nice.NiceCreditInfo;
import com.bank.loan.client.sgi.SgiClient;
import com.bank.loan.client.sgi.SgiGuaranteeInfo;
import com.bank.loan.core.domain.model.LnInquiryResultImpl;
import com.bank.loanapi.model.LnInquiryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class SaitdolInquiryStrategy implements LnInquiryStrategy {

    private final SgiClient sgiClient;

    @Override
    public Long supportedPdId() {
        return 2L;
    }

    @Override
    public CompletableFuture<LnInquiryResult> inquire(Long custId, NiceCreditInfo nice, KcbCreditInfo kcb) {
        return CompletableFuture.supplyAsync(() -> sgiClient.getGuarantee(custId))
                .thenApply(sgi -> applySaitdolRule(nice, kcb, sgi));
    }

    private LnInquiryResult applySaitdolRule(NiceCreditInfo nice, KcbCreditInfo kcb, SgiGuaranteeInfo sgi) {
        return new LnInquiryResultImpl(2L, sgi.getGuaranteeLimit(), sgi.getGuaranteeRate());
    }
}
