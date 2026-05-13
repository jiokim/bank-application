package com.bank.loan.service.application.assessment;

import com.bank.loan.client.kcb.KcbCreditClient;
import com.bank.loan.client.kcb.KcbCreditInfo;
import com.bank.loan.client.nice.NiceCreditClient;
import com.bank.loan.client.nice.NiceCreditInfo;
import com.bank.loan.core.domain.model.LnInquiry;
import com.bank.loan.core.domain.model.LnInquiryCreateSpec;
import com.bank.loan.core.domain.model.LnInquiryResult;
import com.bank.loan.core.domain.repository.LnInquiryRepository;
import com.bank.loan.service.application.assessment.strategy.LnAssessmentStrategyFinder;
import com.bank.loan.service.application.assessment.dto.LoanAssessmentContext;
import com.bank.loan.service.application.assessment.dto.LoanAssessmentCommand;
import com.bank.loan.service.application.assessment.dto.LoanAssessmentInfo;
import com.bank.loan.service.application.assessment.dto.LoanAssessmentResultInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class LnAssessmentService {

    private final NiceCreditClient niceClient;
    private final KcbCreditClient kcbClient;
    private final LnInquiryRepository lnInquiryRepository;
    private final LnAssessmentStrategyFinder strategyFinder;

    public LoanAssessmentInfo assess(LoanAssessmentCommand command) {
        CompletableFuture<NiceCreditInfo> niceFuture =
                CompletableFuture.supplyAsync(() -> niceClient.getNiceCredit(command.getCustId()));
        CompletableFuture<KcbCreditInfo> kcbFuture =
                CompletableFuture.supplyAsync(() -> kcbClient.getKcbCredit(command.getCustId()));

        CompletableFuture.allOf(niceFuture, kcbFuture).join();
        LoanAssessmentContext context = new LoanAssessmentContext(
                command.getCustId(), niceFuture.join(), kcbFuture.join());

        List<CompletableFuture<LnInquiryResult>> futures = command.getPdIds().stream()
                .map(pdId -> strategyFinder.get(pdId).assess(context))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<LnInquiryResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        LnInquiry inquiry = lnInquiryRepository.save(new LnInquiryCreateSpec(command.getCustId(), results));
        return toInfo(inquiry);
    }

    private LoanAssessmentInfo toInfo(LnInquiry inquiry) {
        List<LoanAssessmentResultInfo> items = inquiry.getResults().stream()
                .map(r -> new LoanAssessmentResultInfo(r.getPdId(), r.getMaxLoanAmt(), r.getIntrRt()))
                .toList();
        return new LoanAssessmentInfo(inquiry.getInquiryId(), inquiry.getCustId(), inquiry.getInquiryDt(), items);
    }
}
