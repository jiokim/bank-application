package com.bank.loan.service.application.multistrategy;

import com.bank.loan.client.kcb.KcbCreditInfo;
import com.bank.loan.client.nice.NiceCreditInfo;
import com.bank.loanapi.model.LnInquiryResult;

import java.util.concurrent.CompletableFuture;

public interface LnInquiryStrategy {

    Long supportedPdId();

    CompletableFuture<LnInquiryResult> inquire(Long custId, NiceCreditInfo nice, KcbCreditInfo kcb);
}
