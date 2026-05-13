package com.bank.productapi.model;

import java.math.BigDecimal;

public interface LnPd extends Pd {

    BigDecimal getMaxLoanAmt();

    int getMinAge();

    int getMaxAge();
}
