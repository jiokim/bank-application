package com.bank.loan.controller.dto;

import java.time.LocalDate;
import java.util.List;

public class LoanInquiryResponse {

    private final Long inquiryId;
    private final Long custId;
    private final LocalDate inquiryDt;
    private final List<LoanInquiryResultItem> results;

    public LoanInquiryResponse(Long inquiryId, Long custId, LocalDate inquiryDt, List<LoanInquiryResultItem> results) {
        this.inquiryId = inquiryId;
        this.custId = custId;
        this.inquiryDt = inquiryDt;
        this.results = results;
    }

    public Long getInquiryId() { return inquiryId; }
    public Long getCustId() { return custId; }
    public LocalDate getInquiryDt() { return inquiryDt; }
    public List<LoanInquiryResultItem> getResults() { return results; }
}
