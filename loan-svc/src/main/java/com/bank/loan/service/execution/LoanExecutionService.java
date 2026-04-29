package com.bank.loan.service.execution;

import com.bank.loan.core.domain.model.LnArrCreateSpec;
import com.bank.loan.core.domain.repository.LnInquiryRepository;
import com.bank.loan.core.domain.repository.LoanRepository;
import com.bank.loan.service.dto.LoanApplyCommand;
import com.bank.loan.service.dto.LoanApplyInfo;
import com.bank.loan.core.domain.model.LnArr;
import com.bank.loan.core.domain.model.LnInquiry;
import com.bank.loan.core.domain.model.LnInquiryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LoanExecutionService {

    private final LnInquiryRepository lnInquiryRepository;
    private final LoanRepository loanRepository;

    public LoanApplyInfo apply(LoanApplyCommand command) {
        LnInquiry inquiry = lnInquiryRepository.findById(command.getInquiryId())
                .orElseThrow(() -> new IllegalStateException("한도조회 이력이 없습니다."));

        if (!inquiry.getInquiryDt().equals(LocalDate.now())) {
            throw new IllegalStateException("한도조회가 만료되었습니다. 재조회가 필요합니다.");
        }

        LnInquiryResult result = inquiry.getResults().stream()
                .filter(r -> r.getPdId().equals(command.getPdId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("조회되지 않은 상품입니다: " + command.getPdId()));

        if (command.getLnAmt().compareTo(result.getMaxLoanAmt()) > 0) {
            throw new IllegalStateException("신청금액이 상품 한도를 초과합니다. 한도: " + result.getMaxLoanAmt());
        }

        LocalDate today = LocalDate.now();
        LnArr lnArr = loanRepository.save(new LnArrCreateSpec(
                inquiry.getCustId(),
                result.getPdId(),
                command.getLnAmt(),
                result.getIntrRt(),
                today,
                today.plusYears(1)
        ));

        return new LoanApplyInfo(
                lnArr.getArrId(),
                lnArr.getCustId(),
                lnArr.getPdId(),
                lnArr.getLnAmt(),
                lnArr.getIntrRt(),
                lnArr.getArrSttsCd(),
                lnArr.getArrStrtDt(),
                lnArr.getArrEndDt()
        );
    }
}
