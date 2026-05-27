package com.bank.loan.core.domain.service;

import com.bank.loan.core.domain.model.LnAsm;
import com.bank.loan.core.domain.model.LnAsmCreateSpec;
import com.bank.loan.core.domain.repository.LnAsmRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Component
public class LnAsmMngr {

    private final LnAsmRepository lnAsmRepository;

    public LnAsmMngr(LnAsmRepository lnAsmRepository) {
        this.lnAsmRepository = lnAsmRepository;
    }

    public LnAsm create(LnAsmCreateSpec spec) {
        Long asmId = lnAsmRepository.nextId();
        return lnAsmRepository.save(asmId, spec);
    }

    public LnAsm createGroup(Long custId, LocalDate asmDt) {
        return create(LnAsmCreateSpec.group(custId, asmDt));
    }

    public LnAsm createLoan(Long custId, Long pdId, LocalDate asmDt) {
        return create(LnAsmCreateSpec.loan(custId, pdId, asmDt));
    }

    public LnAsm createReAsm(Long custId, Long pdId, LocalDate asmDt) {
        return create(LnAsmCreateSpec.reAsm(custId, pdId, asmDt));
    }

    public LnAsm approve(Long asmId, BigDecimal maxLoanAmt, BigDecimal intrRt) {
        return lnAsmRepository.approve(asmId, maxLoanAmt, intrRt);
    }

    public LnAsm reject(Long asmId, String rjctRsn) {
        return lnAsmRepository.reject(asmId, rjctRsn);
    }

    public LnAsm updateStatus(Long asmId, String asmStsCd) {
        return lnAsmRepository.updateStatus(asmId, asmStsCd);
    }

    public Optional<LnAsm> findById(Long asmId) {
        return lnAsmRepository.findById(asmId);
    }
}
