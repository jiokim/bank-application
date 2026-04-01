package com.bank.productapi.model;

import java.math.BigDecimal;

public interface Pd {

    Long getPdId();

    String getPdNm();

    BigDecimal getInterestRate();
}
