package com.bank.deposit.core.domain.repository;

import com.bank.deposit.core.domain.model.DpArr;
import com.bank.deposit.core.domain.model.DpArrCreateSpec;

import java.util.List;
import java.util.Optional;

public interface DepositRepository {

    DpArr save(DpArrCreateSpec spec);

    Optional<DpArr> findById(Long arrId);

    List<DpArr> findAll();
}
