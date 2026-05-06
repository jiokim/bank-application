package com.bank.product.domain.model;

import com.bank.productapi.model.DpPd;

import java.math.BigDecimal;

public class DpPdImpl extends PdImpl implements DpPd {

    public DpPdImpl(Long pdId, String pdNm, BigDecimal interestRate) {
        super(pdId, pdNm, interestRate, BigDecimal.ZERO);
    }
}
