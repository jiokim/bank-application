package com.bank.loan.service.application.multistrategy;

import com.bank.loan.client.kcb.KcbCreditInfo;
import com.bank.loan.client.nice.NiceCreditInfo;
import com.bank.loan.core.domain.model.LnInquiryResultImpl;
import com.bank.loanapi.model.LnInquiryResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Component
public class CreditLoanInquiryStrategy implements LnInquiryStrategy {

    @Override
    public String supportedPdId() {
        return "CREDIT_LOAN";
    }

    @Override
    public CompletableFuture<LnInquiryResult> inquire(Long custId, NiceCreditInfo nice, KcbCreditInfo kcb) {
        return CompletableFuture.supplyAsync(() -> applyBankRule(nice, kcb));
    }

    private LnInquiryResult applyBankRule(NiceCreditInfo nice, KcbCreditInfo kcb) {
        int avgScore = (nice.getCreditScore() + kcb.getCreditScore()) / 2;

        if (avgScore >= 700) {
            return new LnInquiryResultImpl("CREDIT_LOAN", new BigDecimal("50000000"), new BigDecimal("0.045"));
        } else {
            return new LnInquiryResultImpl("CREDIT_LOAN", new BigDecimal("20000000"), new BigDecimal("0.065"));
        }
    }
}
