package com.bank.loan.service.dto;

import java.util.List;

public class LoanInquiryCommand {

    private final Long custId;
    private final List<Long> pdIds;

    public LoanInquiryCommand(Long custId, List<Long> pdIds) {
        this.custId = custId;
        this.pdIds = pdIds;
    }

    public Long getCustId() { return custId; }
    public List<Long> getPdIds() { return pdIds; }
}
