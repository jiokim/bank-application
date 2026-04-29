package com.bank.loan.core.domain.model;

import com.bank.loan.core.domain.model.Arr;

import java.math.BigDecimal;

public interface LnArr extends Arr {

    BigDecimal getLnAmt();

    BigDecimal getIntrRt();
}