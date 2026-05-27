package com.bank.cust.core.domain.service;

import com.bank.cust.core.domain.model.Cust;
import com.bank.cust.core.domain.repository.CustRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustMngrImpl implements CustMngr {

    private final CustRepository custRepository;

    @Override
    public Optional<Cust> findCustByRnmNbr(String rnmNbr) {

        return custRepository.findByRnmNbr(rnmNbr);
    }

    @Override
    public Cust create(String custNm, String rnmNbr) {

        return custRepository.saveBrief(custRepository.nextId(), custNm, rnmNbr);
    }
}
