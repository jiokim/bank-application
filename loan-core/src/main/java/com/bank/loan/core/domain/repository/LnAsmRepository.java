package com.bank.loan.core.domain.repository;

import com.bank.loan.core.domain.model.LnAsm;
import com.bank.loan.core.domain.model.LnAsmCreateSpec;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LnAsmRepository {

    Long nextId();

    LnAsm save(Long asmId, LnAsmCreateSpec spec);

    LnAsm updateStatus(Long asmId, String asmStsCd);

    LnAsm approve(Long asmId, BigDecimal maxLoanAmt, BigDecimal intrRt);

    LnAsm reject(Long asmId, String rjctRsn);

    Optional<LnAsm> findById(Long asmId);

    List<LnAsm> findAll();
}
