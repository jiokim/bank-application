package com.bank.loanapi.model;

import java.math.BigDecimal;

public interface LnInquiryResult {

    Long getPdId();

    BigDecimal getMaxLoanAmt();

    BigDecimal getIntrRt();
}
