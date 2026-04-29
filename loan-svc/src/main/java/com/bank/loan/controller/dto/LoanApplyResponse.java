package com.bank.loan.controller.dto;

import com.bank.loanapi.enums.ArrSttsEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanApplyResponse {

    private final Long arrId;
    private final Long custId;
    private final String pdId;
    private final BigDecimal lnAmt;
    private final BigDecimal intrRt;
    private final ArrSttsEnum arrSttsCd;
    private final LocalDate arrStrtDt;
    private final LocalDate arrEndDt;

    public LoanApplyResponse(Long arrId, Long custId, String pdId,
                             BigDecimal lnAmt, BigDecimal intrRt,
                             ArrSttsEnum arrSttsCd, LocalDate arrStrtDt, LocalDate arrEndDt) {
        this.arrId = arrId;
        this.custId = custId;
        this.pdId = pdId;
        this.lnAmt = lnAmt;
        this.intrRt = intrRt;
        this.arrSttsCd = arrSttsCd;
        this.arrStrtDt = arrStrtDt;
        this.arrEndDt = arrEndDt;
    }

    public Long getArrId() { return arrId; }
    public Long getCustId() { return custId; }
    public String getPdId() { return pdId; }
    public BigDecimal getLnAmt() { return lnAmt; }
    public BigDecimal getIntrRt() { return intrRt; }
    public ArrSttsEnum getArrSttsCd() { return arrSttsCd; }
    public LocalDate getArrStrtDt() { return arrStrtDt; }
    public LocalDate getArrEndDt() { return arrEndDt; }
}
