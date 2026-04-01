package com.bank.product.core.model;

import com.bank.productapi.model.Pd;

import java.math.BigDecimal;

public class PdImpl implements Pd {

    public PdImpl() {
    }

    @Override
    public Long getPdId() {
        return 0L;
    }

    @Override
    public String getPdNm() {
        return "";
    }

    @Override
    public BigDecimal getInterestRate() {
        return null;
    }
}
