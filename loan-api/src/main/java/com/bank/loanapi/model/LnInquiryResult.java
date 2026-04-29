package com.bank.loanapi.model;

import java.math.BigDecimal;

public interface LnInquiryResult {

    String getPdId();

    BigDecimal getMaxLoanAmt();

    BigDecimal getIntrRt();
}