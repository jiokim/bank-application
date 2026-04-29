package com.bank.loan.core.domain.model;

import com.bank.loanapi.model.LnInquiryResult;

import java.math.BigDecimal;
import java.util.Objects;

public class LnInquiryResultImpl implements LnInquiryResult {

    private final String pdId;
    private final BigDecimal maxLoanAmt;
    private final BigDecimal intrRt;

    public LnInquiryResultImpl(String pdId, BigDecimal maxLoanAmt, BigDecimal intrRt) {
        this.pdId = Objects.requireNonNull(pdId);
        this.maxLoanAmt = Objects.requireNonNull(maxLoanAmt);
        this.intrRt = Objects.requireNonNull(intrRt);
    }

    @Override public String getPdId() { return pdId; }
    @Override public BigDecimal getMaxLoanAmt() { return maxLoanAmt; }
    @Override public BigDecimal getIntrRt() { return intrRt; }
}