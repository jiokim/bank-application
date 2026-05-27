package com.bank.cust.core.domain.service;

import com.bank.cust.core.domain.model.Cust;
import com.bank.cust.core.domain.model.CustTermsAgreement;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public interface CustMngr {

    /** 고객이 없을 수 있는 조회. 없으면 null 반환. */
    @Nullable
    Cust findCustById(Long custId);

    /** 고객이 반드시 존재해야 하는 조회. 없으면 NoSuchElementException. */
    Cust getCustById(Long custId);

    /** 실명번호로 고객 조회. 실명인증 후 기존 고객 확인 시 사용. */
    Optional<Cust> findCustByRnmNbr(String rnmNbr);

    Cust create(String custNm, String rnmNbr);

    Cust savePhoneNo(Long custId, String phoneNo);

    CustTermsAgreement saveTermsAgreement(Long custId, List<Long> agreedTermIds);
}
