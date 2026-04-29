package com.bank.loan.service.dto;

import java.time.LocalDate;
import java.util.List;

public class LoanInquiryInfo {

    private final Long inquiryId;
    private final Long custId;
    private final LocalDate inquiryDt;
    private final List<LoanInquiryResultInfo> results;

    public LoanInquiryInfo(Long inquiryId, Long custId, LocalDate inquiryDt, List<LoanInquiryResultInfo> results) {
        this.inquiryId = inquiryId;
        this.custId = custId;
        this.inquiryDt = inquiryDt;
        this.results = results;
    }

    public Long getInquiryId() { return inquiryId; }
    public Long getCustId() { return custId; }
    public LocalDate getInquiryDt() { return inquiryDt; }
    public List<LoanInquiryResultInfo> getResults() { return results; }
}
