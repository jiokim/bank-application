package com.bank.loan.service.application.assessment.dto;

import java.time.LocalDate;
import java.util.List;

public class LoanAssessmentInfo {

    private final Long inquiryId;
    private final Long custId;
    private final LocalDate inquiryDt;
    private final List<LoanAssessmentResultInfo> results;

    public LoanAssessmentInfo(Long inquiryId, Long custId, LocalDate inquiryDt, List<LoanAssessmentResultInfo> results) {
        this.inquiryId = inquiryId;
        this.custId = custId;
        this.inquiryDt = inquiryDt;
        this.results = results;
    }

    public Long getInquiryId() { return inquiryId; }
    public Long getCustId() { return custId; }
    public LocalDate getInquiryDt() { return inquiryDt; }
    public List<LoanAssessmentResultInfo> getResults() { return results; }
}
