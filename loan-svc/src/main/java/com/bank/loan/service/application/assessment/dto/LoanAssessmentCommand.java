package com.bank.loan.service.application.assessment.dto;

import java.util.List;

public class LoanAssessmentCommand {

    private final Long custId;
    private final List<Long> pdIds;

    public LoanAssessmentCommand(Long custId, List<Long> pdIds) {
        this.custId = custId;
        this.pdIds = pdIds;
    }

    public Long getCustId() { return custId; }
    public List<Long> getPdIds() { return pdIds; }
}
