package com.bank.loan.service.application;

import com.bank.loan.client.kcb.KcbCreditClient;
import com.bank.loan.client.kcb.KcbCreditInfo;
import com.bank.loan.client.nice.NiceCreditClient;
import com.bank.loan.client.nice.NiceCreditInfo;
import com.bank.loan.core.domain.model.LnInquiryCreateSpec;
import com.bank.loan.core.domain.repository.LnInquiryRepository;
import com.bank.loan.service.application.multistrategy.LnInquiryStrategy;
import com.bank.loan.service.dto.LoanInquiryCommand;
import com.bank.loan.service.dto.LoanInquiryInfo;
import com.bank.loan.service.dto.LoanInquiryResultInfo;
import com.bank.loanapi.model.LnInquiry;
import com.bank.loanapi.model.LnInquiryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final NiceCreditClient niceClient;
    private final KcbCreditClient kcbClient;
    private final LnInquiryRepository lnInquiryRepository;
    private final List<LnInquiryStrategy> strategies;

    public LoanInquiryInfo inquiry(LoanInquiryCommand command) {
        CompletableFuture<NiceCreditInfo> niceFuture =
                CompletableFuture.supplyAsync(() -> niceClient.getNiceCredit(command.getCustId()));
        CompletableFuture<KcbCreditInfo> kcbFuture =
                CompletableFuture.supplyAsync(() -> kcbClient.getKcbCredit(command.getCustId()));

        CompletableFuture.allOf(niceFuture, kcbFuture).join();
        NiceCreditInfo nice = niceFuture.join();
        KcbCreditInfo kcb = kcbFuture.join();

        Map<Long, LnInquiryStrategy> strategyMap = strategies.stream()
                .collect(Collectors.toMap(LnInquiryStrategy::supportedPdId, Function.identity()));

        List<CompletableFuture<LnInquiryResult>> futures = command.getPdIds().stream()
                .map(pdId -> {
                    LnInquiryStrategy strategy = strategyMap.get(pdId);
                    if (strategy == null) {
                        throw new NoSuchElementException("지원하지 않는 상품입니다: " + pdId);
                    }
                    return strategy.inquire(command.getCustId(), nice, kcb);
                })
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<LnInquiryResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        LnInquiry inquiry = lnInquiryRepository.save(new LnInquiryCreateSpec(command.getCustId(), results));

        return toInfo(inquiry);
    }

    private LoanInquiryInfo toInfo(LnInquiry inquiry) {
        List<LoanInquiryResultInfo> items = inquiry.getResults().stream()
                .map(r -> new LoanInquiryResultInfo(r.getPdId(), r.getMaxLoanAmt(), r.getIntrRt()))
                .toList();
        return new LoanInquiryInfo(inquiry.getInquiryId(), inquiry.getCustId(), inquiry.getInquiryDt(), items);
    }
}
