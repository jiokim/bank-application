package com.bank.loan.service.dto;

import com.bank.arrangementapi.enums.ArrSttsEnum;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class LoanApplyResponse {

    private final Long arrId;
    private final Long custId;
    private final Long pdId;
    private final BigDecimal lnAmt;
    private final BigDecimal intrRt;
    private final ArrSttsEnum arrSttsCd;
    private final LocalDate arrStrtDt;
    private final LocalDate arrEndDt;

    public LoanApplyResponse(Long arrId, Long custId, Long pdId,
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
}
