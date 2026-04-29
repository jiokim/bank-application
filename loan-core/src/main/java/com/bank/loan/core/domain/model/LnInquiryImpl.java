package com.bank.loan.core.domain.model;

import com.bank.loan.core.domain.model.LnInquiry;
import com.bank.loan.core.domain.model.LnInquiryResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class LnInquiryImpl implements LnInquiry {

    private final Long inquiryId;
    private final Long custId;
    private final LocalDate inquiryDt;
    private final List<LnInquiryResult> results;

    public LnInquiryImpl(Long inquiryId, Long custId, LocalDate inquiryDt, List<LnInquiryResult> results) {
        this.inquiryId = Objects.requireNonNull(inquiryId);
        this.custId = Objects.requireNonNull(custId);
        this.inquiryDt = Objects.requireNonNull(inquiryDt);
        this.results = Objects.requireNonNull(results);
    }

    @Override public Long getInquiryId() { return inquiryId; }
    @Override public Long getCustId() { return custId; }
    @Override public LocalDate getInquiryDt() { return inquiryDt; }
    @Override public List<LnInquiryResult> getResults() { return results; }
}