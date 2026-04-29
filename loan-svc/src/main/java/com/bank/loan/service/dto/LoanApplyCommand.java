package com.bank.loan.service.dto;

import java.math.BigDecimal;

public class LoanApplyCommand {

    private final Long inquiryId;
    private final String pdId;
    private final BigDecimal lnAmt;

    public LoanApplyCommand(Long inquiryId, String pdId, BigDecimal lnAmt) {
        this.inquiryId = inquiryId;
        this.pdId = pdId;
        this.lnAmt = lnAmt;
    }

    public Long getInquiryId() { return inquiryId; }
    public String getPdId() { return pdId; }
    public BigDecimal getLnAmt() { return lnAmt; }
}
