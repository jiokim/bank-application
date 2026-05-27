package com.bank.loan.core.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class LnAsmImpl implements LnAsm {

    private final Long asmId;
    private final String asmKndCd;
    private final String asmStsCd;
    private final Long custId;
    private final Long pdId;
    private final LocalDate asmDt;
    private final BigDecimal maxLoanAmt;
    private final BigDecimal intrRt;
    private final String rjctRsn;

    public LnAsmImpl(Long asmId, String asmKndCd, String asmStsCd, Long custId, Long pdId,
                     LocalDate asmDt, BigDecimal maxLoanAmt, BigDecimal intrRt, String rjctRsn) {
        this.asmId = Objects.requireNonNull(asmId);
        this.asmKndCd = Objects.requireNonNull(asmKndCd);
        this.asmStsCd = Objects.requireNonNull(asmStsCd);
        this.custId = Objects.requireNonNull(custId);
        this.pdId = pdId;
        this.asmDt = Objects.requireNonNull(asmDt);
        this.maxLoanAmt = maxLoanAmt == null ? BigDecimal.ZERO : maxLoanAmt;
        this.intrRt = intrRt == null ? BigDecimal.ZERO : intrRt;
        this.rjctRsn = rjctRsn;
    }

    @Override public Long getAsmId() { return asmId; }
    @Override public String getAsmKndCd() { return asmKndCd; }
    @Override public String getAsmStsCd() { return asmStsCd; }
    @Override public Long getCustId() { return custId; }
    @Override public Long getPdId() { return pdId; }
    @Override public LocalDate getAsmDt() { return asmDt; }
    @Override public BigDecimal getMaxLoanAmt() { return maxLoanAmt; }
    @Override public BigDecimal getIntrRt() { return intrRt; }
    @Override public String getRjctRsn() { return rjctRsn; }
}
