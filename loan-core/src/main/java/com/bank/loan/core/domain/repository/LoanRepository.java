package com.bank.loan.core.domain.repository;

import com.bank.loan.core.domain.model.LnArrCreateSpec;
import com.bank.loan.core.domain.model.LnArr;

import java.util.List;
import java.util.Optional;

public interface LoanRepository {

    LnArr save(LnArrCreateSpec spec);

    Optional<LnArr> findById(Long arrId);

    List<LnArr> findAll();
}
