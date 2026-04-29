package com.bank.loan.core.domain.model;

import java.time.LocalDate;
import java.util.List;

public interface LnInquiry {

    Long getInquiryId();

    Long getCustId();

    LocalDate getInquiryDt();

    List<LnInquiryResult> getResults();
}