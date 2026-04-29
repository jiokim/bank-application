package com.bank.loan.core.domain.model;

import com.bank.loanapi.enums.ArrSttsEnum;
import com.bank.loanapi.enums.ArrTpEnum;
import com.bank.loanapi.model.LnArr;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class LnArrImpl extends ArrImpl implements LnArr {

    private final BigDecimal lnAmt;
    private final BigDecimal intrRt;

    public LnArrImpl(Long arrId, Long custId, String pdId,
                     BigDecimal lnAmt, BigDecimal intrRt,
                     ArrSttsEnum arrSttsCd, LocalDate arrStrtDt, LocalDate arrEndDt) {
        super(arrId, ArrTpEnum.LN, custId, arrSttsCd, arrStrtDt, arrEndDt, pdId);
        this.lnAmt = Objects.requireNonNull(lnAmt);
        this.intrRt = Objects.requireNonNull(intrRt);
    }

    @Override public BigDecimal getLnAmt() { return lnAmt; }
    @Override public BigDecimal getIntrRt() { return intrRt; }
}
