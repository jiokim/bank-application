package com.bank.cust.core.domain.repository;

import com.bank.cust.core.domain.model.Cust;

import java.util.Optional;

public interface CustRepository {
    Cust save(Long custId, int age);

    Cust saveBrief(Long custId, String custNm, String rnmNbr);

    Optional<Cust> findById(Long custId);

    Optional<Cust> findByRnmNbr(String rnmNbr);

    Cust savePhoneNo(Long custId, String phoneNo);

    Long nextId();
}
