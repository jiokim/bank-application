package com.bank.loan.service.dto;

import com.bank.arrangement.core.domain.enums.ArrSttsEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanApplyInfo {

    private final Long arrId;
    private final Long custId;
    private final Long pdId;
    private final BigDecimal lnAmt;
    private final BigDecimal intrRt;
    private final ArrSttsEnum arrSttsCd;
    private final LocalDate arrStrtDt;
    private final LocalDate arrEndDt;

    public LoanApplyInfo(Long arrId, Long custId, Long pdId,
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
    public Long getPdId() { return pdId; }
    public BigDecimal getLnAmt() { return lnAmt; }
    public BigDecimal getIntrRt() { return intrRt; }
    public ArrSttsEnum getArrSttsCd() { return arrSttsCd; }
    public LocalDate getArrStrtDt() { return arrStrtDt; }
    public LocalDate getArrEndDt() { return arrEndDt; }
}
