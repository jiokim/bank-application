package com.bank.loan.service.dto;

import java.math.BigDecimal;

public class LoanApplyCommand {

    private final Long inquiryId;
    private final Long pdId;
    private final BigDecimal lnAmt;

    public LoanApplyCommand(Long inquiryId, Long pdId, BigDecimal lnAmt) {
        this.inquiryId = inquiryId;
        this.pdId = pdId;
        this.lnAmt = lnAmt;
    }

    public Long getInquiryId() { return inquiryId; }
    public Long getPdId() { return pdId; }
    public BigDecimal getLnAmt() { return lnAmt; }
}
