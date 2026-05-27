package com.bank.cust.core.domain.repository;

import com.bank.cust.core.domain.model.CustTermsAgreement;

import java.util.List;

public interface CustTermsAgreementRepository {

    CustTermsAgreement save(Long custId, List<Long> agreedTermIds);
}