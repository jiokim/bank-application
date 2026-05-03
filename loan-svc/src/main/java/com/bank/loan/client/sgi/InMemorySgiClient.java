package com.bank.loan.client.sgi;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class InMemorySgiClient implements SgiClient {

    @Override
    public SgiGuaranteeInfo getGuarantee(Long custId) {
        return new SgiGuaranteeInfo(custId, new BigDecimal("20000000"), new BigDecimal("0.039"));
    }
}
