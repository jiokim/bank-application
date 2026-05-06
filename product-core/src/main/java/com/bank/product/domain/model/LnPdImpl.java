package com.bank.product.domain.model;

import com.bank.productapi.model.LnPd;

import java.math.BigDecimal;

public class LnPdImpl extends PdImpl implements LnPd {

    public LnPdImpl(Long pdId, String pdNm, BigDecimal interestRate, BigDecimal maxLoanAmt) {
        super(pdId, pdNm, interestRate, maxLoanAmt);
    }
}
