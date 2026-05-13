package com.bank.loan.core.domain.service;

import com.bank.arrangement.core.domain.enums.ArrSttsEnum;
import com.bank.loan.core.domain.repository.LoanRepository;
import org.springframework.stereotype.Component;

@Component
public class LnArrMngrImpl implements LnArrMngr {

    private final LoanRepository loanRepository;

    public LnArrMngrImpl(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public boolean hasInProgressArr(Long custId) {
        return loanRepository.findAll().stream()
                .anyMatch(arr -> arr.getCustId().equals(custId)
                        && arr.getArrSttsCd() == ArrSttsEnum.IN_PROGRESS);
    }

    @Override
    public boolean hasActiveArr(Long custId, Long pdId) {
        return loanRepository.findAll().stream()
                .anyMatch(arr -> arr.getCustId().equals(custId)
                        && arr.getPdId().equals(pdId)
                        && arr.getArrSttsCd() == ArrSttsEnum.ACTIVE);
    }
}
