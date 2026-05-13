package com.bank.product.domain.model;

import com.bank.productapi.model.LnPd;

import java.math.BigDecimal;

public class LnPdImpl extends PdImpl implements LnPd {

    private final int minAge;
    private final int maxAge;

    public LnPdImpl(Long pdId, String pdNm, BigDecimal interestRate, BigDecimal maxLoanAmt,
                    int minAge, int maxAge) {
        super(pdId, pdNm, interestRate, maxLoanAmt);
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    @Override public int getMinAge() { return minAge; }
    @Override public int getMaxAge() { return maxAge; }
}
