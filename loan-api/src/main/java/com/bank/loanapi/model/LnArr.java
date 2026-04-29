package com.bank.loanapi.model;

import com.bank.loanapi.model.Arr;

import java.math.BigDecimal;

public interface LnArr extends Arr {

    BigDecimal getLnAmt();

    BigDecimal getIntrRt();
}