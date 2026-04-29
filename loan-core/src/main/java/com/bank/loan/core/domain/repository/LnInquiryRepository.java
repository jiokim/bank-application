package com.bank.loan.core.domain.repository;

import com.bank.loan.core.domain.model.LnInquiryCreateSpec;
import com.bank.loan.core.domain.model.LnInquiry;

import java.util.Optional;

public interface LnInquiryRepository {

    LnInquiry save(LnInquiryCreateSpec spec);

    Optional<LnInquiry> findById(Long inquiryId);
}