package com.bank.loanapi.model;

import com.bank.arrangementapi.model.Arr;

import java.math.BigDecimal;

public interface LnArr extends Arr {

    BigDecimal getLnAmt();

    BigDecimal getIntrRt();
}