package com.bank.loan.core.domain.model;

import java.math.BigDecimal;

public interface LnInquiryResult {

    Long getPdId();

    BigDecimal getMaxLoanAmt();

    BigDecimal getIntrRt();
}
