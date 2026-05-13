package com.bank.cust.core.domain.repository;

import com.bank.cust.core.domain.model.Cust;

import java.util.Optional;

public interface CustRepository {
    Cust save(Long custId, int age);
    Optional<Cust> findById(Long custId);
}
