package com.bank.cust.core.domain.service;

import com.bank.cust.core.domain.model.Cust;

import java.util.Optional;

public interface CustMngr {
    Optional<Cust> findCustByRnmNbr(String rnmNbr);

    Cust create(String custNm, String rnmNbr);
}
