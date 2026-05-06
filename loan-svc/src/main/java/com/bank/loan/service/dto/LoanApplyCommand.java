package com.bank.loan.service.dto;

import java.math.BigDecimal;

public class LoanApplyCommand {

    private final Long inquiryId;
    private final Long pdId;
    private final BigDecimal lnAmt;
    private final String custRrn;

    public LoanApplyCommand(Long inquiryId, Long pdId, BigDecimal lnAmt, String custRrn) {
        this.inquiryId = inquiryId;
        this.pdId = pdId;
        this.lnAmt = lnAmt;
        this.custRrn = custRrn;
    }

    public Long getInquiryId() { return inquiryId; }
    public Long getPdId() { return pdId; }
    public BigDecimal getLnAmt() { return lnAmt; }
    public String getCustRrn() { return custRrn; }
}
