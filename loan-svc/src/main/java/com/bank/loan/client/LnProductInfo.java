package com.bank.loan.client;

import java.math.BigDecimal;

public class LnProductInfo {

    private final String pdId;
    private final BigDecimal maxLoanAmt;
    private final BigDecimal intrRt;

    public LnProductInfo(String pdId, BigDecimal maxLoanAmt, BigDecimal intrRt) {
        this.pdId = pdId;
        this.maxLoanAmt = maxLoanAmt;
        this.intrRt = intrRt;
    }

    public String getPdId() { return pdId; }
    public BigDecimal getMaxLoanAmt() { return maxLoanAmt; }
    public BigDecimal getIntrRt() { return intrRt; }
}