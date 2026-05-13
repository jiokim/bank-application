package com.bank.cust.core.domain.model;

import java.util.Objects;

public record CustImpl(Long custId, int age) implements Cust {

    public CustImpl {
        Objects.requireNonNull(custId);
    }

    @Override public Long getCustId() { return custId; }
    @Override public int getAge()     { return age; }
}
