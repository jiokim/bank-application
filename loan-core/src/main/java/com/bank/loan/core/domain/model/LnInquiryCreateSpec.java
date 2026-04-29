package com.bank.loan.core.domain.model;

import com.bank.loanapi.model.LnInquiryResult;

import java.util.List;
import java.util.Objects;

public class LnInquiryCreateSpec {

    private final Long custId;
    private final List<LnInquiryResult> results;

    public LnInquiryCreateSpec(Long custId, List<LnInquiryResult> results) {
        this.custId = Objects.requireNonNull(custId);
        this.results = Objects.requireNonNull(results);
    }

    public Long getCustId() { return custId; }
    public List<LnInquiryResult> getResults() { return results; }
}