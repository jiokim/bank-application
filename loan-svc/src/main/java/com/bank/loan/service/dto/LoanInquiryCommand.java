package com.bank.loan.service.dto;

import java.util.List;

public class LoanInquiryCommand {

    private final Long custId;
    private final List<String> pdIds;

    public LoanInquiryCommand(Long custId, List<String> pdIds) {
        this.custId = custId;
        this.pdIds = pdIds;
    }

    public Long getCustId() { return custId; }
    public List<String> getPdIds() { return pdIds; }
}
