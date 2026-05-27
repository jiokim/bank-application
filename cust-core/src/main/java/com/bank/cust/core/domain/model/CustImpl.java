package com.bank.cust.core.domain.model;

import java.util.Objects;

public record CustImpl(Long custId, int age, String custNm, String rnmNbr, String phoneNo) implements Cust {

    public CustImpl(Long custId, int age) {
        this(custId, age, null, null, null);
    }

    public CustImpl(Long custId, int age, String custNm, String rnmNbr) {
        this(custId, age, custNm, rnmNbr, null);
    }

    public CustImpl {
        Objects.requireNonNull(custId);
    }

    @Override public Long getCustId()    { return custId; }
    @Override public int getAge()        { return age; }
    @Override public String getCustNm()  { return custNm; }
    @Override public String getRnmNbr()  { return rnmNbr; }
    @Override public String getPhoneNo() { return phoneNo; }
}
