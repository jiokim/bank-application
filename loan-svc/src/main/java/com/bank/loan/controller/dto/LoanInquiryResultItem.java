package com.bank.loan.controller.dto;

import java.math.BigDecimal;

public class LoanInquiryResultItem {

    private final Long pdId;
    private final BigDecimal maxLoanAmt;
    private final BigDecimal intrRt;

    public LoanInquiryResultItem(Long pdId, BigDecimal maxLoanAmt, BigDecimal intrRt) {
        this.pdId = pdId;
        this.maxLoanAmt = maxLoanAmt;
        this.intrRt = intrRt;
    }

    public Long getPdId() { return pdId; }
    public BigDecimal getMaxLoanAmt() { return maxLoanAmt; }
    public BigDecimal getIntrRt() { return intrRt; }
}
