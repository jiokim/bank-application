package com.bank.productapi.model;

import java.math.BigDecimal;

public interface Pd {

    String getPdId();

    String getPdNm();

    BigDecimal getInterestRate();

    BigDecimal getMaxLoanAmt();
}
