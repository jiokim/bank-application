package com.bank.product.domain.model;

import com.bank.productapi.model.Pd;

import java.math.BigDecimal;
import java.util.Objects;

public class PdImpl implements Pd {

    private final Long pdId;
    private final String pdNm;
    private final BigDecimal interestRate;
    private final BigDecimal maxLoanAmt;

    public PdImpl(Long pdId, String pdNm, BigDecimal interestRate, BigDecimal maxLoanAmt) {
        this.pdId = Objects.requireNonNull(pdId, "pdId must not be null");
        this.pdNm = Objects.requireNonNull(pdNm, "pdNm must not be null");
        this.interestRate = Objects.requireNonNull(interestRate, "interestRate must not be null");
        this.maxLoanAmt = Objects.requireNonNull(maxLoanAmt, "maxLoanAmt must not be null");
    }

    @Override
    public Long getPdId() {
        return pdId;
    }

    @Override
    public String getPdNm() {
        return pdNm;
    }

    @Override
    public BigDecimal getInterestRate() {
        return interestRate;
    }

    @Override
    public BigDecimal getMaxLoanAmt() {
        return maxLoanAmt;
    }
}
