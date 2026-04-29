package com.bank.loan.client.kcb;

import org.springframework.stereotype.Component;

@Component
public class InMemoryKcbCreditClient implements KcbCreditClient {

    @Override
    public KcbCreditInfo getKcbCredit(Long custId) {
        return new KcbCreditInfo(custId, 720, 0);
    }
}
