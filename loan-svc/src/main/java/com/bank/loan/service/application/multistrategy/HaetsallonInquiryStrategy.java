package com.bank.loan.service.application.multistrategy;

import com.bank.loan.client.kcb.KcbCreditInfo;
import com.bank.loan.client.kifa.KifaClient;
import com.bank.loan.client.kifa.KifaGuaranteeInfo;
import com.bank.loan.client.nice.NiceCreditInfo;
import com.bank.loan.core.domain.model.LnInquiryResultImpl;
import com.bank.loan.core.domain.model.LnInquiryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class HaetsallonInquiryStrategy implements LnInquiryStrategy {

    private final KifaClient kifaClient;

    @Override
    public Long supportedPdId() {
        return 3L;
    }

    @Override
    public CompletableFuture<LnInquiryResult> inquire(Long custId, NiceCreditInfo nice, KcbCreditInfo kcb) {
        return CompletableFuture.supplyAsync(() -> kifaClient.getGuarantee(custId))
                .thenApply(kifa -> applyHaetsallonRule(nice, kcb, kifa));
    }

    private LnInquiryResult applyHaetsallonRule(NiceCreditInfo nice, KcbCreditInfo kcb, KifaGuaranteeInfo kifa) {
        return new LnInquiryResultImpl(3L, kifa.getGuaranteeLimit(), kifa.getGuaranteeRate());
    }
}
