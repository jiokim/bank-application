package com.bank.loan.core.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class LnArrCreateSpec {

    private final Long custId;
    private final Long pdId;
    private final BigDecimal lnAmt;
    private final BigDecimal intrRt;
    private final LocalDate arrStrtDt;
    private final LocalDate arrEndDt;

    public LnArrCreateSpec(Long custId, Long pdId, BigDecimal lnAmt, BigDecimal intrRt,
                           LocalDate arrStrtDt, LocalDate arrEndDt) {
        this.custId = Objects.requireNonNull(custId);
        this.pdId = Objects.requireNonNull(pdId);
        this.lnAmt = Objects.requireNonNull(lnAmt);
        this.intrRt = Objects.requireNonNull(intrRt);
        this.arrStrtDt = Objects.requireNonNull(arrStrtDt);
        this.arrEndDt = Objects.requireNonNull(arrEndDt);
    }

    public Long getCustId() { return custId; }
    public Long getPdId() { return pdId; }
    public BigDecimal getLnAmt() { return lnAmt; }
    public BigDecimal getIntrRt() { return intrRt; }
    public LocalDate getArrStrtDt() { return arrStrtDt; }
    public LocalDate getArrEndDt() { return arrEndDt; }
}
