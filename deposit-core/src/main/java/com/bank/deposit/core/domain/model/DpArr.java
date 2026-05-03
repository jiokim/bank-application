package com.bank.deposit.core.domain.model;

import com.bank.arrangement.core.domain.model.Arr;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DpArr extends Arr {

    BigDecimal getDpAmt();

    BigDecimal getIntrRt();

    LocalDate getMaturityDt();
}
