package com.bank.cust.core.domain.model;

public interface Cust {
    Long getCustId();

    int getAge();

    String getCustNm();

    String getRnmNbr();

    String getPhoneNo();

    default boolean isAgeEligible(int minAge, int maxAge) {
        return getAge() >= minAge && getAge() <= maxAge;
    }
}
