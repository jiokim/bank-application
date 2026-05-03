package com.bank.loan.service.dto;

import java.math.BigDecimal;

public class LoanInquiryResultInfo {

    private final Long pdId;
    private final BigDecimal maxLoanAmt;
    private final BigDecimal intrRt;

    public LoanInquiryResultInfo(Long pdId, BigDecimal maxLoanAmt, BigDecimal intrRt) {
        this.pdId = pdId;
        this.maxLoanAmt = maxLoanAmt;
        this.intrRt = intrRt;
    }

    public Long getPdId() { return pdId; }
    public BigDecimal getMaxLoanAmt() { return maxLoanAmt; }
    public BigDecimal getIntrRt() { return intrRt; }
}
