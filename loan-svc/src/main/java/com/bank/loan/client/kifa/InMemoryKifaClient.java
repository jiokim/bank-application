package com.bank.loan.client.kifa;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class InMemoryKifaClient implements KifaClient {

    @Override
    public KifaGuaranteeInfo getGuarantee(Long custId) {
        return new KifaGuaranteeInfo(custId, new BigDecimal("15000000"), new BigDecimal("0.035"));
    }
}
