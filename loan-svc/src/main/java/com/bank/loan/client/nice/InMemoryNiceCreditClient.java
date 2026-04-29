package com.bank.loan.client.nice;

import org.springframework.stereotype.Component;

@Component
public class InMemoryNiceCreditClient implements NiceCreditClient {

    @Override
    public NiceCreditInfo getNiceCredit(Long custId) {
        return new NiceCreditInfo(custId, 750, 0);
    }
}
