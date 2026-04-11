package com.bank.loan.service;

import com.bank.loan.core.domain.model.LnArrCreateSpec;
import com.bank.loan.core.domain.repository.LoanRepository;
import com.bank.loan.service.dto.LoanApplyRequest;
import com.bank.loan.service.dto.LoanApplyResponse;
import com.bank.loanapi.model.LnArr;
import com.bank.productapi.model.Pd;
import com.bank.productapi.model.PdMngr;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LoanCommandService {

    private final LoanRepository loanRepository;
    private final PdMngr pdMngr;

    public LoanApplyResponse apply(LoanApplyRequest request) {
        Pd pd = pdMngr.getPd(request.getPdId());

        if (request.getLnAmt().compareTo(pd.getMaxLoanAmt()) > 0) {
            throw new IllegalStateException("신청금액이 상품 한도를 초과합니다. 한도: " + pd.getMaxLoanAmt());
        }

        LocalDate today = LocalDate.now();
        LnArr lnArr = loanRepository.save(new LnArrCreateSpec(
                request.getCustId(),
                pd.getPdId(),
                request.getLnAmt(),
                pd.getInterestRate(),
                today,
                today.plusYears(1)
        ));

        return new LoanApplyResponse(
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
